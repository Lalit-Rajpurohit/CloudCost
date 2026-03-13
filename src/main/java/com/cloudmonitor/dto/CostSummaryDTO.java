package com.cloudmonitor.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * DTO for monthly cost summary including total and breakdown by service.
 */
public class CostSummaryDTO {

    private YearMonth month;
    private BigDecimal totalCost;
    private String currency;
    private List<CostByServiceDTO> serviceBreakdown;
    private int numberOfServices;

    public CostSummaryDTO() {}

    public CostSummaryDTO(YearMonth month, BigDecimal totalCost, String currency,
                          List<CostByServiceDTO> serviceBreakdown, int numberOfServices) {
        this.month = month;
        this.totalCost = totalCost;
        this.currency = currency;
        this.serviceBreakdown = serviceBreakdown;
        this.numberOfServices = numberOfServices;
    }

    public static CostSummaryDTO fromServiceBreakdown(
            YearMonth month, List<CostByServiceDTO> breakdown, String currency) {

        BigDecimal total = breakdown.stream()
                .map(CostByServiceDTO::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CostSummaryDTO(month, total, currency, breakdown, breakdown.size());
    }

    public YearMonth getMonth() { return month; }
    public void setMonth(YearMonth month) { this.month = month; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public List<CostByServiceDTO> getServiceBreakdown() { return serviceBreakdown; }
    public void setServiceBreakdown(List<CostByServiceDTO> serviceBreakdown) { this.serviceBreakdown = serviceBreakdown; }

    public int getNumberOfServices() { return numberOfServices; }
    public void setNumberOfServices(int numberOfServices) { this.numberOfServices = numberOfServices; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private YearMonth month;
        private BigDecimal totalCost;
        private String currency;
        private List<CostByServiceDTO> serviceBreakdown;
        private int numberOfServices;

        public Builder month(YearMonth month) { this.month = month; return this; }
        public Builder totalCost(BigDecimal totalCost) { this.totalCost = totalCost; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder serviceBreakdown(List<CostByServiceDTO> serviceBreakdown) { this.serviceBreakdown = serviceBreakdown; return this; }
        public Builder numberOfServices(int numberOfServices) { this.numberOfServices = numberOfServices; return this; }

        public CostSummaryDTO build() {
            return new CostSummaryDTO(month, totalCost, currency, serviceBreakdown, numberOfServices);
        }
    }
}
