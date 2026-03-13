package com.cloudmonitor.controller;

import com.cloudmonitor.dto.CostSummaryDTO;
import com.cloudmonitor.dto.FetchResultDTO;
import com.cloudmonitor.dto.ServiceTrendDTO;
import com.cloudmonitor.model.CostData;
import com.cloudmonitor.scheduler.CostFetchScheduler;
import com.cloudmonitor.service.CostExplorerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * REST Controller for cost data operations.
 *
 * Provides endpoints to:
 * - Get monthly cost summaries
 * - Get service cost trends
 * - Trigger manual cost data fetches
 * - View today's cost data
 */
@RestController
@RequestMapping("/api/cost")
@CrossOrigin(origins = "*") // Allow frontend access; restrict in production
public class CostController {

    private static final Logger log = LoggerFactory.getLogger(CostController.class);

    private final CostExplorerService costExplorerService;
    private final CostFetchScheduler costFetchScheduler;

    public CostController(CostExplorerService costExplorerService,
                          CostFetchScheduler costFetchScheduler) {
        this.costExplorerService = costExplorerService;
        this.costFetchScheduler = costFetchScheduler;
    }

    /**
     * Get monthly cost summary with breakdown by service.
     *
     * Example: GET /api/cost/monthly?year=2026&month=3
     *
     * @param year the year (e.g., 2026)
     * @param month the month (1-12)
     * @return CostSummaryDTO with total and service breakdown
     */
    @GetMapping("/monthly")
    public ResponseEntity<CostSummaryDTO> getMonthlySummary(
            @RequestParam int year,
            @RequestParam int month) {

        log.info("Fetching monthly summary for {}-{}", year, month);

        if (month < 1 || month > 12) {
            return ResponseEntity.badRequest().build();
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        CostSummaryDTO summary = costExplorerService.getMonthlySummary(yearMonth);

        return ResponseEntity.ok(summary);
    }

    /**
     * Get cost trend for a specific service.
     *
     * Example: GET /api/cost/service-trend?service=Amazon EC2&start=2026-02-01&end=2026-02-28
     *
     * @param service the AWS service name (URL encoded if contains spaces)
     * @param start start date (inclusive)
     * @param end end date (inclusive)
     * @return ServiceTrendDTO with daily costs and statistics
     */
    @GetMapping("/service-trend")
    public ResponseEntity<ServiceTrendDTO> getServiceTrend(
            @RequestParam String service,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        log.info("Fetching trend for service '{}' from {} to {}", service, start, end);

        if (start.isAfter(end)) {
            return ResponseEntity.badRequest().build();
        }

        ServiceTrendDTO trend = costExplorerService.getServiceTrend(service, start, end);
        return ResponseEntity.ok(trend);
    }

    /**
     * Trigger an immediate cost data fetch from AWS.
     *
     * Example: POST /api/cost/fetch?start=2026-03-01&end=2026-03-02
     *
     * Note: In production, this endpoint should require authentication.
     *
     * @param start start date (inclusive)
     * @param end end date (inclusive)
     * @return FetchResultDTO with operation results
     */
    @PostMapping("/fetch")
    public ResponseEntity<FetchResultDTO> triggerFetch(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        log.info("Manual fetch triggered for {} to {}", start, end);

        if (start.isAfter(end)) {
            return ResponseEntity.badRequest()
                    .body(FetchResultDTO.error(start, end, "Start date must be before end date"));
        }

        // Validate date range (AWS allows up to 12 months back)
        LocalDate earliestAllowed = LocalDate.now().minusMonths(12);
        if (start.isBefore(earliestAllowed)) {
            return ResponseEntity.badRequest()
                    .body(FetchResultDTO.error(start, end,
                            "Start date cannot be more than 12 months in the past"));
        }

        FetchResultDTO result = costFetchScheduler.triggerManualFetch(start, end);
        return ResponseEntity.ok(result);
    }

    /**
     * Get today's cost data (if available).
     *
     * Example: GET /api/cost/today
     *
     * @return List of CostData records for today
     */
    @GetMapping("/today")
    public ResponseEntity<List<CostData>> getTodayCosts() {
        log.info("Fetching today's cost data");

        List<CostData> todayCosts = costExplorerService.getTodayCosts();
        return ResponseEntity.ok(todayCosts);
    }

    /**
     * Get list of all services that have cost data.
     *
     * Example: GET /api/cost/services
     *
     * @return List of service names
     */
    @GetMapping("/services")
    public ResponseEntity<List<String>> getAvailableServices() {
        log.info("Fetching available services");

        List<String> services = costExplorerService.getAvailableServices();
        return ResponseEntity.ok(services);
    }

    /**
     * Get cost data for a specific date range.
     *
     * Example: GET /api/cost/range?start=2026-03-01&end=2026-03-07
     *
     * @param start start date (inclusive)
     * @param end end date (inclusive)
     * @return CostSummaryDTO for the date range
     */
    @GetMapping("/range")
    public ResponseEntity<CostSummaryDTO> getCostsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        log.info("Fetching costs for range {} to {}", start, end);

        if (start.isAfter(end)) {
            return ResponseEntity.badRequest().build();
        }

        // For simplicity, treat the range as a month starting from start date
        YearMonth month = YearMonth.from(start);
        CostSummaryDTO summary = costExplorerService.getMonthlySummary(month);

        return ResponseEntity.ok(summary);
    }
}
