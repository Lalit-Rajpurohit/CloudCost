package com.cloudmonitor.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for service cost trend over time.
 */
public class ServiceTrendDTO {

    private String serviceName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String currency;
    private BigDecimal totalCost;
    private BigDecimal averageDailyCost;
    private BigDecimal minDailyCost;
    private BigDecimal maxDailyCost;
    private List<CostByServiceDTO.DailyCost> dailyCosts;

    public ServiceTrendDTO() {}

    public ServiceTrendDTO(String serviceName, LocalDate startDate, LocalDate endDate,
                           String currency, BigDecimal totalCost, BigDecimal averageDailyCost,
                           BigDecimal minDailyCost, BigDecimal maxDailyCost,
                           List<CostByServiceDTO.DailyCost> dailyCosts) {
        this.serviceName = serviceName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.currency = currency;
        this.totalCost = totalCost;
        this.averageDailyCost = averageDailyCost;
        this.minDailyCost = minDailyCost;
        this.maxDailyCost = maxDailyCost;
        this.dailyCosts = dailyCosts;
    }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public BigDecimal getAverageDailyCost() { return averageDailyCost; }
    public void setAverageDailyCost(BigDecimal averageDailyCost) { this.averageDailyCost = averageDailyCost; }

    public BigDecimal getMinDailyCost() { return minDailyCost; }
    public void setMinDailyCost(BigDecimal minDailyCost) { this.minDailyCost = minDailyCost; }

    public BigDecimal getMaxDailyCost() { return maxDailyCost; }
    public void setMaxDailyCost(BigDecimal maxDailyCost) { this.maxDailyCost = maxDailyCost; }

    public List<CostByServiceDTO.DailyCost> getDailyCosts() { return dailyCosts; }
    public void setDailyCosts(List<CostByServiceDTO.DailyCost> dailyCosts) { this.dailyCosts = dailyCosts; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String serviceName;
        private LocalDate startDate;
        private LocalDate endDate;
        private String currency;
        private BigDecimal totalCost;
        private BigDecimal averageDailyCost;
        private BigDecimal minDailyCost;
        private BigDecimal maxDailyCost;
        private List<CostByServiceDTO.DailyCost> dailyCosts;

        public Builder serviceName(String serviceName) { this.serviceName = serviceName; return this; }
        public Builder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public Builder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder totalCost(BigDecimal totalCost) { this.totalCost = totalCost; return this; }
        public Builder averageDailyCost(BigDecimal averageDailyCost) { this.averageDailyCost = averageDailyCost; return this; }
        public Builder minDailyCost(BigDecimal minDailyCost) { this.minDailyCost = minDailyCost; return this; }
        public Builder maxDailyCost(BigDecimal maxDailyCost) { this.maxDailyCost = maxDailyCost; return this; }
        public Builder dailyCosts(List<CostByServiceDTO.DailyCost> dailyCosts) { this.dailyCosts = dailyCosts; return this; }

        public ServiceTrendDTO build() {
            return new ServiceTrendDTO(serviceName, startDate, endDate, currency, totalCost,
                    averageDailyCost, minDailyCost, maxDailyCost, dailyCosts);
        }
    }
}
