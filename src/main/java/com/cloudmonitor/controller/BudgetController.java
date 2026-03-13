package com.cloudmonitor.controller;

import com.cloudmonitor.dto.BudgetDTO;
import com.cloudmonitor.model.Budget;
import com.cloudmonitor.service.AlertService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for budget management.
 *
 * Provides CRUD operations for budget thresholds
 * and manual budget evaluation triggers.
 */
@RestController
@RequestMapping("/api/budget")
@CrossOrigin(origins = "*")
public class BudgetController {

    private static final Logger log = LoggerFactory.getLogger(BudgetController.class);

    private final AlertService alertService;

    public BudgetController(AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * Get all budgets.
     *
     * Example: GET /api/budget
     */
    @GetMapping
    public ResponseEntity<List<Budget>> getAllBudgets() {
        log.info("Fetching all budgets");
        return ResponseEntity.ok(alertService.getAllBudgets());
    }

    /**
     * Get a budget by ID.
     *
     * Example: GET /api/budget/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<Budget> getBudgetById(@PathVariable Long id) {
        log.info("Fetching budget with id {}", id);

        return alertService.getBudgetById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new budget.
     *
     * Example: POST /api/budget
     * Body: {"name": "Monthly Limit", "monthlyThreshold": 500.00, "emailRecipient": "user@example.com"}
     */
    @PostMapping
    public ResponseEntity<Budget> createBudget(@Valid @RequestBody BudgetDTO budgetDTO) {
        log.info("Creating new budget: {}", budgetDTO.getName());

        try {
            Budget budget = alertService.createBudget(budgetDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(budget);
        } catch (IllegalArgumentException e) {
            log.warn("Budget creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update an existing budget.
     *
     * Example: PUT /api/budget/1
     * Body: {"monthlyThreshold": 600.00}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Budget> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetDTO budgetDTO) {

        log.info("Updating budget with id {}", id);

        try {
            Budget budget = alertService.updateBudget(id, budgetDTO);
            return ResponseEntity.ok(budget);
        } catch (IllegalArgumentException e) {
            log.warn("Budget update failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a budget.
     *
     * Example: DELETE /api/budget/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        log.info("Deleting budget with id {}", id);

        alertService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Manually trigger budget evaluation.
     *
     * Example: POST /api/budget/evaluate
     */
    @PostMapping("/evaluate")
    public ResponseEntity<String> evaluateBudgets() {
        log.info("Manual budget evaluation triggered");

        alertService.evaluateBudgets();
        return ResponseEntity.ok("Budget evaluation completed");
    }
}
