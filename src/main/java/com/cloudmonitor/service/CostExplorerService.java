package com.cloudmonitor.service;

import com.cloudmonitor.dto.CostByServiceDTO;
import com.cloudmonitor.dto.CostSummaryDTO;
import com.cloudmonitor.dto.FetchResultDTO;
import com.cloudmonitor.dto.ServiceTrendDTO;
import com.cloudmonitor.model.CostData;
import com.cloudmonitor.repository.CostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;
import software.amazon.awssdk.services.costexplorer.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for interacting with AWS Cost Explorer API and managing cost data.
 *
 * This service handles:
 * - Fetching cost data from AWS Cost Explorer
 * - Parsing and storing cost data in the database
 * - Aggregating and querying historical cost data
 */
@Service
public class CostExplorerService {

    private static final Logger log = LoggerFactory.getLogger(CostExplorerService.class);
    private static final DateTimeFormatter AWS_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final String DEFAULT_CURRENCY = "USD";

    private final CostExplorerClient costExplorerClient;
    private final CostRepository costRepository;

    public CostExplorerService(CostExplorerClient costExplorerClient,
                               CostRepository costRepository) {
        this.costExplorerClient = costExplorerClient;
        this.costRepository = costRepository;
    }

    /**
     * Fetches cost data from AWS Cost Explorer for the given date range
     * and stores it in the database.
     *
     * AWS Cost Explorer uses exclusive end dates, so to get data for March 1st,
     * you need to specify end date as March 2nd.
     *
     * @param startDate inclusive start date
     * @param endDate inclusive end date (will be converted to exclusive for AWS API)
     * @return FetchResultDTO with operation results
     */
    @Transactional
    public FetchResultDTO fetchAndStoreDailyCosts(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching AWS cost data from {} to {}", startDate, endDate);

        try {
            // AWS end date is exclusive, so add one day
            LocalDate awsEndDate = endDate.plusDays(1);

            // Build the Cost Explorer request
            GetCostAndUsageRequest request = GetCostAndUsageRequest.builder()
                    .timePeriod(DateInterval.builder()
                            .start(startDate.format(AWS_DATE_FORMAT))
                            .end(awsEndDate.format(AWS_DATE_FORMAT))
                            .build())
                    .granularity(Granularity.DAILY)
                    .metrics("UnblendedCost")
                    .groupBy(GroupDefinition.builder()
                            .type(GroupDefinitionType.DIMENSION)
                            .key("SERVICE")
                            .build())
                    .build();

            // Execute the API call
            GetCostAndUsageResponse response = costExplorerClient.getCostAndUsage(request);

            // Parse and store the results
            List<CostData> costDataList = parseResponse(response);

            int savedCount = 0;
            int updatedCount = 0;

            for (CostData costData : costDataList) {
                // Check if record already exists (for idempotency)
                Optional<CostData> existing = costRepository.findByDateAndServiceName(
                        costData.getDate(), costData.getServiceName());

                if (existing.isPresent()) {
                    // Update existing record
                    CostData existingData = existing.get();
                    existingData.setAmount(costData.getAmount());
                    existingData.setCurrency(costData.getCurrency());
                    existingData.setUsageType(costData.getUsageType());
                    costRepository.save(existingData);
                    updatedCount++;
                } else {
                    // Insert new record
                    costRepository.save(costData);
                    savedCount++;
                }
            }

            log.info("Stored {} new records, updated {} existing records",
                    savedCount, updatedCount);

            return FetchResultDTO.success(startDate, endDate,
                    costDataList.size(), savedCount, updatedCount);

        } catch (CostExplorerException e) {
            log.error("AWS Cost Explorer API error: {}", e.awsErrorDetails().errorMessage(), e);
            return FetchResultDTO.error(startDate, endDate,
                    "AWS API Error: " + e.awsErrorDetails().errorMessage());
        } catch (Exception e) {
            log.error("Error fetching cost data: {}", e.getMessage(), e);
            return FetchResultDTO.error(startDate, endDate,
                    "Error: " + e.getMessage());
        }
    }

    /**
     * Parses the AWS GetCostAndUsageResponse into CostData entities.
     */
    private List<CostData> parseResponse(GetCostAndUsageResponse response) {
        List<CostData> result = new ArrayList<>();

        if (response.resultsByTime() == null || response.resultsByTime().isEmpty()) {
            log.warn("Empty response from Cost Explorer");
            return result;
        }

        for (ResultByTime resultByTime : response.resultsByTime()) {
            LocalDate date = LocalDate.parse(
                    resultByTime.timePeriod().start(), AWS_DATE_FORMAT);

            if (resultByTime.groups() == null || resultByTime.groups().isEmpty()) {
                log.debug("No groups for date {}", date);
                continue;
            }

            for (Group group : resultByTime.groups()) {
                // Get service name from group keys
                String serviceName = group.keys().isEmpty() ? "Unknown" : group.keys().get(0);

                // Get cost metrics
                Map<String, MetricValue> metrics = group.metrics();
                MetricValue unblendedCost = metrics.get("UnblendedCost");

                if (unblendedCost != null) {
                    BigDecimal amount = new BigDecimal(unblendedCost.amount())
                            .setScale(6, RoundingMode.HALF_UP);
                    String currency = unblendedCost.unit();

                    // Skip zero-cost entries to reduce database size
                    if (amount.compareTo(BigDecimal.ZERO) > 0) {
                        CostData costData = new CostData(
                                date,
                                serviceName,
                                null, // usageType not fetched in this query
                                amount,
                                currency != null ? currency : DEFAULT_CURRENCY
                        );
                        result.add(costData);
                    }
                }
            }
        }

        log.debug("Parsed {} cost records from AWS response", result.size());
        return result;
    }

    /**
     * Gets the monthly cost summary from stored data.
     *
     * @param month the year and month to summarize
     * @return CostSummaryDTO with total and breakdown by service
     */
    public CostSummaryDTO getMonthlySummary(YearMonth month) {
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        log.debug("Getting monthly summary for {} ({} to {})", month, startDate, endDate);

        List<CostData> monthData = costRepository.findByDateBetween(startDate, endDate);

        if (monthData.isEmpty()) {
            return CostSummaryDTO.builder()
                    .month(month)
                    .totalCost(BigDecimal.ZERO)
                    .currency(DEFAULT_CURRENCY)
                    .serviceBreakdown(Collections.emptyList())
                    .numberOfServices(0)
                    .build();
        }

        // Group by service and calculate totals
        Map<String, BigDecimal> serviceToTotal = monthData.stream()
                .collect(Collectors.groupingBy(
                        CostData::getServiceName,
                        Collectors.reducing(BigDecimal.ZERO,
                                CostData::getAmount,
                                BigDecimal::add)));

        BigDecimal grandTotal = serviceToTotal.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get currency from first record
        String currency = monthData.get(0).getCurrency();

        // Build service breakdown DTOs
        List<CostByServiceDTO> breakdown = serviceToTotal.entrySet().stream()
                .map(entry -> {
                    CostByServiceDTO dto = CostByServiceDTO.builder()
                            .serviceName(entry.getKey())
                            .totalCost(entry.getValue().setScale(2, RoundingMode.HALF_UP))
                            .currency(currency)
                            .build();
                    dto.calculatePercentage(grandTotal);
                    return dto;
                })
                .sorted((a, b) -> b.getTotalCost().compareTo(a.getTotalCost()))
                .collect(Collectors.toList());

        return CostSummaryDTO.builder()
                .month(month)
                .totalCost(grandTotal.setScale(2, RoundingMode.HALF_UP))
                .currency(currency)
                .serviceBreakdown(breakdown)
                .numberOfServices(breakdown.size())
                .build();
    }

    /**
     * Gets the daily cost trend for a specific service.
     *
     * @param serviceName the AWS service name
     * @param startDate start of the trend period
     * @param endDate end of the trend period
     * @return ServiceTrendDTO with daily costs and statistics
     */
    public ServiceTrendDTO getServiceTrend(String serviceName,
                                           LocalDate startDate, LocalDate endDate) {
        log.debug("Getting trend for service {} from {} to {}",
                serviceName, startDate, endDate);

        List<CostData> serviceData = costRepository.findByServiceNameAndDateBetween(
                serviceName, startDate, endDate);

        if (serviceData.isEmpty()) {
            return ServiceTrendDTO.builder()
                    .serviceName(serviceName)
                    .startDate(startDate)
                    .endDate(endDate)
                    .currency(DEFAULT_CURRENCY)
                    .totalCost(BigDecimal.ZERO)
                    .averageDailyCost(BigDecimal.ZERO)
                    .minDailyCost(BigDecimal.ZERO)
                    .maxDailyCost(BigDecimal.ZERO)
                    .dailyCosts(Collections.emptyList())
                    .build();
        }

        // Build daily cost list
        List<CostByServiceDTO.DailyCost> dailyCosts = serviceData.stream()
                .sorted(Comparator.comparing(CostData::getDate))
                .map(cd -> CostByServiceDTO.DailyCost.builder()
                        .date(cd.getDate())
                        .amount(cd.getAmount().setScale(2, RoundingMode.HALF_UP))
                        .build())
                .collect(Collectors.toList());

        // Calculate statistics
        BigDecimal total = serviceData.stream()
                .map(CostData::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal average = total.divide(
                new BigDecimal(serviceData.size()), 2, RoundingMode.HALF_UP);

        BigDecimal min = serviceData.stream()
                .map(CostData::getAmount)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal max = serviceData.stream()
                .map(CostData::getAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        String currency = serviceData.get(0).getCurrency();

        return ServiceTrendDTO.builder()
                .serviceName(serviceName)
                .startDate(startDate)
                .endDate(endDate)
                .currency(currency)
                .totalCost(total.setScale(2, RoundingMode.HALF_UP))
                .averageDailyCost(average)
                .minDailyCost(min.setScale(2, RoundingMode.HALF_UP))
                .maxDailyCost(max.setScale(2, RoundingMode.HALF_UP))
                .dailyCosts(dailyCosts)
                .build();
    }

    /**
     * Gets cost data for today (if available).
     */
    public List<CostData> getTodayCosts() {
        return costRepository.findByDate(LocalDate.now());
    }

    /**
     * Gets all distinct service names in the database.
     */
    public List<String> getAvailableServices() {
        return costRepository.findDistinctServiceNames();
    }

    /**
     * Gets total cost for a date range.
     */
    public BigDecimal getTotalCost(LocalDate startDate, LocalDate endDate) {
        return costRepository.getTotalCostBetween(startDate, endDate);
    }
}
