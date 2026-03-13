package com.cloudmonitor.scheduler;

import com.cloudmonitor.dto.FetchResultDTO;
import com.cloudmonitor.service.AlertService;
import com.cloudmonitor.service.CostExplorerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Scheduled task to fetch AWS cost data daily.
 *
 * Runs at 01:00 every day to fetch the previous day's costs.
 * This timing allows AWS to finalize the previous day's billing data.
 *
 * The scheduler is idempotent - running multiple times for the same
 * date will update existing records rather than creating duplicates.
 */
@Component
public class CostFetchScheduler {

    private static final Logger log = LoggerFactory.getLogger(CostFetchScheduler.class);

    private final CostExplorerService costExplorerService;
    private final AlertService alertService;

    @Value("${scheduler.cost-fetch.enabled:true}")
    private boolean schedulerEnabled;

    public CostFetchScheduler(CostExplorerService costExplorerService,
                              AlertService alertService) {
        this.costExplorerService = costExplorerService;
        this.alertService = alertService;
    }

    /**
     * Daily scheduled task to fetch yesterday's costs.
     *
     * Cron expression: "0 0 1 * * ?" means:
     * - Second: 0
     * - Minute: 0
     * - Hour: 1 (1 AM)
     * - Day of month: * (every day)
     * - Month: * (every month)
     * - Day of week: ? (any)
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void fetchYesterdayCosts() {
        if (!schedulerEnabled) {
            log.debug("Scheduler is disabled, skipping cost fetch");
            return;
        }

        log.info("Starting scheduled cost fetch for yesterday's data");

        LocalDate yesterday = LocalDate.now().minusDays(1);

        try {
            FetchResultDTO result = costExplorerService.fetchAndStoreDailyCosts(yesterday, yesterday);

            if ("SUCCESS".equals(result.getStatus())) {
                log.info("Scheduled fetch completed: {} records fetched, {} saved, {} updated",
                        result.getRecordsFetched(),
                        result.getRecordsSaved(),
                        result.getRecordsUpdated());

                // After storing new data, evaluate budgets for alerts
                evaluateBudgetsAfterFetch();
            } else {
                log.error("Scheduled fetch failed: {}", result.getMessage());
            }

        } catch (Exception e) {
            log.error("Unexpected error during scheduled cost fetch: {}", e.getMessage(), e);
        }
    }

    /**
     * Evaluates budgets after fetching new cost data.
     */
    private void evaluateBudgetsAfterFetch() {
        try {
            log.info("Evaluating budgets after cost data fetch");
            alertService.evaluateBudgets();
        } catch (Exception e) {
            log.error("Error evaluating budgets: {}", e.getMessage(), e);
            // Don't rethrow - budget evaluation failure shouldn't fail the fetch
        }
    }

    /**
     * Manually trigger a cost fetch (for testing or backfill).
     * This method is called by the controller.
     */
    public FetchResultDTO triggerManualFetch(LocalDate startDate, LocalDate endDate) {
        log.info("Manual cost fetch triggered for {} to {}", startDate, endDate);

        FetchResultDTO result = costExplorerService.fetchAndStoreDailyCosts(startDate, endDate);

        if ("SUCCESS".equals(result.getStatus())) {
            // Optionally evaluate budgets after manual fetch
            evaluateBudgetsAfterFetch();
        }

        return result;
    }

    /**
     * Backfill historical data for a date range.
     * Useful for initial setup or recovering missing data.
     */
    public FetchResultDTO backfillHistoricalData(LocalDate startDate, LocalDate endDate) {
        log.info("Starting historical data backfill from {} to {}", startDate, endDate);

        // AWS Cost Explorer supports fetching up to 12 months of historical data
        LocalDate earliestAllowed = LocalDate.now().minusMonths(12);
        if (startDate.isBefore(earliestAllowed)) {
            log.warn("Start date {} is before the 12-month limit, adjusting to {}",
                    startDate, earliestAllowed);
            startDate = earliestAllowed;
        }

        return costExplorerService.fetchAndStoreDailyCosts(startDate, endDate);
    }
}
