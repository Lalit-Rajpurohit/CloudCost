package com.cloudmonitor.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for cost breakdown by service.
 */
public class CostByServiceDTO {

    private String serviceName;
    private BigDecimal totalCost;
    private String currency;
    private BigDecimal percentageOfTotal;
    private List<DailyCost> dailyCosts;

    public CostByServiceDTO() {}

    public CostByServiceDTO(String serviceName, BigDecimal totalCost, String currency,
                            BigDecimal percentageOfTotal, List<DailyCost> dailyCosts) {
        this.serviceName = serviceName;
        this.totalCost = totalCost;
        this.currency = currency;
        this.percentageOfTotal = percentageOfTotal;
        this.dailyCosts = dailyCosts;
    }

    public void calculatePercentage(BigDecimal grandTotal) {
        if (grandTotal != null && grandTotal.compareTo(BigDecimal.ZERO) > 0) {
            this.percentageOfTotal = this.totalCost
                    .multiply(new BigDecimal("100"))
                    .divide(grandTotal, 2, RoundingMode.HALF_UP);
        } else {
            this.percentageOfTotal = BigDecimal.ZERO;
        }
    }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getPercentageOfTotal() { return percentageOfTotal; }
    public void setPercentageOfTotal(BigDecimal percentageOfTotal) { this.percentageOfTotal = percentageOfTotal; }

    public List<DailyCost> getDailyCosts() { return dailyCosts; }
    public void setDailyCosts(List<DailyCost> dailyCosts) { this.dailyCosts = dailyCosts; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String serviceName;
        private BigDecimal totalCost;
        private String currency;
        private BigDecimal percentageOfTotal;
        private List<DailyCost> dailyCosts;

        public Builder serviceName(String serviceName) { this.serviceName = serviceName; return this; }
        public Builder totalCost(BigDecimal totalCost) { this.totalCost = totalCost; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder percentageOfTotal(BigDecimal percentageOfTotal) { this.percentageOfTotal = percentageOfTotal; return this; }
        public Builder dailyCosts(List<DailyCost> dailyCosts) { this.dailyCosts = dailyCosts; return this; }

        public CostByServiceDTO build() {
            return new CostByServiceDTO(serviceName, totalCost, currency, percentageOfTotal, dailyCosts);
        }
    }

    /**
     * Inner class for daily cost data points.
     */
    public static class DailyCost {
        private LocalDate date;
        private BigDecimal amount;

        public DailyCost() {}

        public DailyCost(LocalDate date, BigDecimal amount) {
            this.date = date;
            this.amount = amount;
        }

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private LocalDate date;
            private BigDecimal amount;

            public Builder date(LocalDate date) { this.date = date; return this; }
            public Builder amount(BigDecimal amount) { this.amount = amount; return this; }

            public DailyCost build() {
                return new DailyCost(date, amount);
            }
        }
    }
}
