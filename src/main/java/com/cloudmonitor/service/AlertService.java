package com.cloudmonitor.service;

import com.cloudmonitor.dto.BudgetDTO;
import com.cloudmonitor.model.Budget;
import com.cloudmonitor.repository.BudgetRepository;
import com.cloudmonitor.repository.CostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Service for budget alerting and notifications.
 *
 * Monitors actual costs against configured budget thresholds
 * and sends email alerts when thresholds are exceeded.
 */
@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final BudgetRepository budgetRepository;
    private final CostRepository costRepository;
    private final JavaMailSender mailSender;

    @Value("${alert.budget.monthly-threshold:100.00}")
    private BigDecimal defaultMonthlyThreshold;

    @Value("${alert.budget.email-recipient:admin@example.com}")
    private String defaultEmailRecipient;

    @Value("${spring.mail.username:noreply@cloudmonitor.local}")
    private String mailFrom;

    public AlertService(BudgetRepository budgetRepository,
                        CostRepository costRepository,
                        JavaMailSender mailSender) {
        this.budgetRepository = budgetRepository;
        this.costRepository = costRepository;
        this.mailSender = mailSender;
    }

    /**
     * Evaluates all active budgets against current month's costs.
     * Sends alerts for any exceeded budgets.
     */
    @Transactional
    public void evaluateBudgets() {
        log.info("Evaluating budgets against current month costs");

        YearMonth currentMonth = YearMonth.now();
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate today = LocalDate.now();

        // Get current month's total cost
        BigDecimal currentMonthCost = costRepository.getTotalCostBetween(monthStart, today);
        log.info("Current month total cost: ${}", currentMonthCost.setScale(2, RoundingMode.HALF_UP));

        // Check all active budgets
        List<Budget> activeBudgets = budgetRepository.findByIsActiveTrue();

        if (activeBudgets.isEmpty()) {
            // Use default budget from properties
            checkDefaultBudget(currentMonthCost);
            return;
        }

        for (Budget budget : activeBudgets) {
            checkBudget(budget, currentMonthCost);
        }
    }

    /**
     * Checks if the default budget threshold is exceeded.
     */
    private void checkDefaultBudget(BigDecimal currentCost) {
        if (currentCost.compareTo(defaultMonthlyThreshold) > 0) {
            log.warn("Default budget threshold exceeded! Current: ${}, Threshold: ${}",
                    currentCost.setScale(2, RoundingMode.HALF_UP),
                    defaultMonthlyThreshold);

            sendAlertEmail(
                    defaultEmailRecipient,
                    "Default Budget",
                    currentCost,
                    defaultMonthlyThreshold
            );
        }
    }

    /**
     * Checks if a specific budget threshold is exceeded.
     */
    private void checkBudget(Budget budget, BigDecimal currentCost) {
        if (currentCost.compareTo(budget.getMonthlyThreshold()) > 0) {
            log.warn("Budget '{}' threshold exceeded! Current: ${}, Threshold: ${}",
                    budget.getName(),
                    currentCost.setScale(2, RoundingMode.HALF_UP),
                    budget.getMonthlyThreshold());

            // Check if we already sent an alert today to avoid spamming
            if (shouldSendAlert(budget)) {
                String recipient = budget.getEmailRecipient() != null
                        ? budget.getEmailRecipient()
                        : defaultEmailRecipient;

                sendAlertEmail(recipient, budget.getName(), currentCost, budget.getMonthlyThreshold());

                // Update last alert sent timestamp
                budget.setLastAlertSent(LocalDateTime.now());
                budgetRepository.save(budget);
            }
        }
    }

    /**
     * Determines if an alert should be sent (avoid duplicate alerts same day).
     */
    private boolean shouldSendAlert(Budget budget) {
        if (budget.getLastAlertSent() == null) {
            return true;
        }
        // Only send one alert per day per budget
        return budget.getLastAlertSent().toLocalDate().isBefore(LocalDate.now());
    }

    /**
     * Sends a budget alert email.
     */
    private void sendAlertEmail(String recipient, String budgetName,
                                BigDecimal currentCost, BigDecimal threshold) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(recipient);
            message.setSubject("[Cloud Cost Monitor] Budget Alert: " + budgetName);
            message.setText(String.format(
                    """
                    BUDGET ALERT

                    Budget: %s
                    Current Month Cost: $%s
                    Budget Threshold: $%s
                    Overage: $%s (%.1f%% over budget)

                    Please review your AWS usage to avoid unexpected charges.

                    --
                    Cloud Cost Monitor
                    """,
                    budgetName,
                    currentCost.setScale(2, RoundingMode.HALF_UP),
                    threshold.setScale(2, RoundingMode.HALF_UP),
                    currentCost.subtract(threshold).setScale(2, RoundingMode.HALF_UP),
                    calculateOveragePercentage(currentCost, threshold)
            ));

            mailSender.send(message);
            log.info("Budget alert email sent to {} for budget '{}'", recipient, budgetName);

        } catch (MailException e) {
            log.error("Failed to send alert email: {}", e.getMessage());
            // Don't throw - alerting failure shouldn't break the application
        }
    }

    private double calculateOveragePercentage(BigDecimal current, BigDecimal threshold) {
        if (threshold.compareTo(BigDecimal.ZERO) == 0) {
            return 100.0;
        }
        return current.subtract(threshold)
                .divide(threshold, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .doubleValue();
    }

    /**
     * Creates a new budget.
     */
    @Transactional
    public Budget createBudget(BudgetDTO dto) {
        if (budgetRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Budget with name '" + dto.getName() + "' already exists");
        }

        Budget budget = new Budget();
        budget.setName(dto.getName());
        budget.setMonthlyThreshold(dto.getMonthlyThreshold());
        budget.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "USD");
        budget.setEmailRecipient(dto.getEmailRecipient());
        budget.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        return budgetRepository.save(budget);
    }

    /**
     * Updates an existing budget.
     */
    @Transactional
    public Budget updateBudget(Long id, BudgetDTO dto) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found with id: " + id));

        if (dto.getName() != null) {
            budget.setName(dto.getName());
        }
        if (dto.getMonthlyThreshold() != null) {
            budget.setMonthlyThreshold(dto.getMonthlyThreshold());
        }
        if (dto.getCurrency() != null) {
            budget.setCurrency(dto.getCurrency());
        }
        if (dto.getEmailRecipient() != null) {
            budget.setEmailRecipient(dto.getEmailRecipient());
        }
        if (dto.getIsActive() != null) {
            budget.setIsActive(dto.getIsActive());
        }

        return budgetRepository.save(budget);
    }

    /**
     * Gets all budgets.
     */
    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }

    /**
     * Gets a budget by ID.
     */
    public Optional<Budget> getBudgetById(Long id) {
        return budgetRepository.findById(id);
    }

    /**
     * Deletes a budget.
     */
    @Transactional
    public void deleteBudget(Long id) {
        budgetRepository.deleteById(id);
    }
}
