package com.cloudmonitor.repository;

import com.cloudmonitor.model.CostData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for CostData entity operations.
 */
@Repository
public interface CostRepository extends JpaRepository<CostData, Long> {

    /**
     * Find all cost records within a date range.
     */
    List<CostData> findByDateBetween(LocalDate start, LocalDate end);

    /**
     * Find cost records for a specific service within a date range.
     */
    List<CostData> findByServiceNameAndDateBetween(
            String serviceName, LocalDate start, LocalDate end);

    /**
     * Find all cost records for a specific date.
     */
    List<CostData> findByDate(LocalDate date);

    /**
     * Find a specific cost record by date and service name.
     * Used for idempotent upsert operations.
     */
    Optional<CostData> findByDateAndServiceName(LocalDate date, String serviceName);

    /**
     * Get distinct service names in the database.
     */
    @Query("SELECT DISTINCT c.serviceName FROM CostData c ORDER BY c.serviceName")
    List<String> findDistinctServiceNames();

    /**
     * Get total cost for a date range.
     */
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM CostData c WHERE c.date BETWEEN :start AND :end")
    java.math.BigDecimal getTotalCostBetween(
            @Param("start") LocalDate start, @Param("end") LocalDate end);

    /**
     * Check if data exists for a specific date.
     */
    boolean existsByDate(LocalDate date);
}
