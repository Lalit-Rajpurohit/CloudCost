package com.cloudmonitor.repository;

import com.cloudmonitor.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Budget entity operations.
 */
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    /**
     * Find all active budgets.
     */
    List<Budget> findByIsActiveTrue();

    /**
     * Find a budget by name.
     */
    Optional<Budget> findByName(String name);

    /**
     * Check if a budget with the given name exists.
     */
    boolean existsByName(String name);
}
