package com.cloudmonitor.service;

import com.cloudmonitor.dto.CostSummaryDTO;
import com.cloudmonitor.dto.FetchResultDTO;
import com.cloudmonitor.dto.ServiceTrendDTO;
import com.cloudmonitor.model.CostData;
import com.cloudmonitor.repository.CostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;
import software.amazon.awssdk.services.costexplorer.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CostExplorerService.
 *
 * Tests focus on:
 * - Correct parsing of AWS Cost Explorer responses
 * - Proper aggregation of cost data
 * - Handling of edge cases (empty responses, zero costs)
 */
@ExtendWith(MockitoExtension.class)
class CostExplorerServiceTest {

    @Mock
    private CostExplorerClient costExplorerClient;

    @Mock
    private CostRepository costRepository;

    private CostExplorerService costExplorerService;

    @BeforeEach
    void setUp() {
        costExplorerService = new CostExplorerService(costExplorerClient, costRepository);
    }

    @Test
    @DisplayName("Should parse AWS response and store cost data correctly")
    void testFetchAndStoreDailyCosts_Success() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 3, 1);
        LocalDate endDate = LocalDate.of(2026, 3, 1);

        GetCostAndUsageResponse mockResponse = createMockResponse(
                "2026-03-01",
                Map.of("Amazon EC2", "25.50", "Amazon S3", "5.25")
        );

        when(costExplorerClient.getCostAndUsage(any(GetCostAndUsageRequest.class)))
                .thenReturn(mockResponse);
        when(costRepository.findByDateAndServiceName(any(), any()))
                .thenReturn(Optional.empty());
        when(costRepository.save(any(CostData.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        FetchResultDTO result = costExplorerService.fetchAndStoreDailyCosts(startDate, endDate);

        // Assert
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(2, result.getRecordsFetched());
        assertEquals(2, result.getRecordsSaved());
        assertEquals(0, result.getRecordsUpdated());

        // Verify save was called for each service
        verify(costRepository, times(2)).save(any(CostData.class));
    }

    @Test
    @DisplayName("Should update existing records instead of creating duplicates")
    void testFetchAndStoreDailyCosts_UpdateExisting() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 3, 1);

        GetCostAndUsageResponse mockResponse = createMockResponse(
                "2026-03-01",
                Map.of("Amazon EC2", "30.00")
        );

        CostData existingData = new CostData(
                1L, date, "Amazon EC2", null, new BigDecimal("25.00"), "USD"
        );

        when(costExplorerClient.getCostAndUsage(any(GetCostAndUsageRequest.class)))
                .thenReturn(mockResponse);
        when(costRepository.findByDateAndServiceName(date, "Amazon EC2"))
                .thenReturn(Optional.of(existingData));
        when(costRepository.save(any(CostData.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        FetchResultDTO result = costExplorerService.fetchAndStoreDailyCosts(date, date);

        // Assert
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(1, result.getRecordsFetched());
        assertEquals(0, result.getRecordsSaved());
        assertEquals(1, result.getRecordsUpdated());

        // Verify the existing record was updated with new amount
        ArgumentCaptor<CostData> captor = ArgumentCaptor.forClass(CostData.class);
        verify(costRepository).save(captor.capture());
        assertEquals(new BigDecimal("30.000000"), captor.getValue().getAmount());
    }

    @Test
    @DisplayName("Should handle empty AWS response gracefully")
    void testFetchAndStoreDailyCosts_EmptyResponse() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 3, 1);

        GetCostAndUsageResponse emptyResponse = GetCostAndUsageResponse.builder()
                .resultsByTime(Collections.emptyList())
                .build();

        when(costExplorerClient.getCostAndUsage(any(GetCostAndUsageRequest.class)))
                .thenReturn(emptyResponse);

        // Act
        FetchResultDTO result = costExplorerService.fetchAndStoreDailyCosts(date, date);

        // Assert
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(0, result.getRecordsFetched());
        verify(costRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should skip zero-cost entries")
    void testFetchAndStoreDailyCosts_SkipsZeroCosts() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 3, 1);

        GetCostAndUsageResponse mockResponse = createMockResponse(
                "2026-03-01",
                Map.of("Amazon EC2", "25.00", "Amazon Route53", "0.00")
        );

        when(costExplorerClient.getCostAndUsage(any(GetCostAndUsageRequest.class)))
                .thenReturn(mockResponse);
        when(costRepository.findByDateAndServiceName(any(), any()))
                .thenReturn(Optional.empty());
        when(costRepository.save(any(CostData.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        FetchResultDTO result = costExplorerService.fetchAndStoreDailyCosts(date, date);

        // Assert
        assertEquals(1, result.getRecordsFetched()); // Only EC2, not Route53
        verify(costRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should calculate monthly summary correctly")
    void testGetMonthlySummary() {
        // Arrange
        YearMonth month = YearMonth.of(2026, 3);
        List<CostData> mockData = Arrays.asList(
                new CostData(1L, LocalDate.of(2026, 3, 1), "Amazon EC2", null,
                        new BigDecimal("10.00"), "USD"),
                new CostData(2L, LocalDate.of(2026, 3, 2), "Amazon EC2", null,
                        new BigDecimal("15.00"), "USD"),
                new CostData(3L, LocalDate.of(2026, 3, 1), "Amazon S3", null,
                        new BigDecimal("5.00"), "USD")
        );

        when(costRepository.findByDateBetween(
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31)))
                .thenReturn(mockData);

        // Act
        CostSummaryDTO summary = costExplorerService.getMonthlySummary(month);

        // Assert
        assertEquals(month, summary.getMonth());
        assertEquals(new BigDecimal("30.00"), summary.getTotalCost());
        assertEquals(2, summary.getNumberOfServices());

        // Verify EC2 total is 25.00
        var ec2 = summary.getServiceBreakdown().stream()
                .filter(s -> s.getServiceName().equals("Amazon EC2"))
                .findFirst()
                .orElseThrow();
        assertEquals(new BigDecimal("25.00"), ec2.getTotalCost());
    }

    @Test
    @DisplayName("Should return empty summary for months with no data")
    void testGetMonthlySummary_NoData() {
        // Arrange
        YearMonth month = YearMonth.of(2026, 3);
        when(costRepository.findByDateBetween(any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        CostSummaryDTO summary = costExplorerService.getMonthlySummary(month);

        // Assert
        assertEquals(BigDecimal.ZERO, summary.getTotalCost());
        assertEquals(0, summary.getNumberOfServices());
        assertTrue(summary.getServiceBreakdown().isEmpty());
    }

    @Test
    @DisplayName("Should calculate service trend with statistics")
    void testGetServiceTrend() {
        // Arrange
        String serviceName = "Amazon EC2";
        LocalDate start = LocalDate.of(2026, 3, 1);
        LocalDate end = LocalDate.of(2026, 3, 3);

        List<CostData> mockData = Arrays.asList(
                new CostData(1L, LocalDate.of(2026, 3, 1), serviceName, null,
                        new BigDecimal("10.00"), "USD"),
                new CostData(2L, LocalDate.of(2026, 3, 2), serviceName, null,
                        new BigDecimal("20.00"), "USD"),
                new CostData(3L, LocalDate.of(2026, 3, 3), serviceName, null,
                        new BigDecimal("15.00"), "USD")
        );

        when(costRepository.findByServiceNameAndDateBetween(serviceName, start, end))
                .thenReturn(mockData);

        // Act
        ServiceTrendDTO trend = costExplorerService.getServiceTrend(serviceName, start, end);

        // Assert
        assertEquals(serviceName, trend.getServiceName());
        assertEquals(new BigDecimal("45.00"), trend.getTotalCost());
        assertEquals(new BigDecimal("15.00"), trend.getAverageDailyCost());
        assertEquals(new BigDecimal("10.00"), trend.getMinDailyCost());
        assertEquals(new BigDecimal("20.00"), trend.getMaxDailyCost());
        assertEquals(3, trend.getDailyCosts().size());
    }

    @Test
    @DisplayName("Should handle AWS API errors gracefully")
    void testFetchAndStoreDailyCosts_AWSError() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 3, 1);

        CostExplorerException awsException = (CostExplorerException) CostExplorerException.builder()
                .message("Access Denied")
                .awsErrorDetails(software.amazon.awssdk.awscore.exception.AwsErrorDetails.builder()
                        .errorCode("AccessDeniedException")
                        .errorMessage("User is not authorized")
                        .build())
                .build();

        when(costExplorerClient.getCostAndUsage(any(GetCostAndUsageRequest.class)))
                .thenThrow(awsException);

        // Act
        FetchResultDTO result = costExplorerService.fetchAndStoreDailyCosts(date, date);

        // Assert
        assertEquals("ERROR", result.getStatus());
        assertTrue(result.getMessage().contains("AWS API Error"));
    }

    /**
     * Helper method to create mock AWS Cost Explorer responses.
     */
    private GetCostAndUsageResponse createMockResponse(String date, Map<String, String> serviceCosts) {
        List<Group> groups = new ArrayList<>();

        for (Map.Entry<String, String> entry : serviceCosts.entrySet()) {
            MetricValue metricValue = MetricValue.builder()
                    .amount(entry.getValue())
                    .unit("USD")
                    .build();

            Group group = Group.builder()
                    .keys(entry.getKey())
                    .metrics(Map.of("UnblendedCost", metricValue))
                    .build();

            groups.add(group);
        }

        // AWS end date is next day
        String endDate = LocalDate.parse(date).plusDays(1).toString();

        ResultByTime resultByTime = ResultByTime.builder()
                .timePeriod(DateInterval.builder()
                        .start(date)
                        .end(endDate)
                        .build())
                .groups(groups)
                .build();

        return GetCostAndUsageResponse.builder()
                .resultsByTime(resultByTime)
                .build();
    }
}
