package com.cloudmonitor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * DTO for creating/updating budget thresholds.
 */
public class BudgetDTO {

    private Long id;

    @NotBlank(message = "Budget name is required")
    private String name;

    @NotNull(message = "Monthly threshold is required")
    @Positive(message = "Monthly threshold must be positive")
    private BigDecimal monthlyThreshold;

    private String currency = "USD";

    @Email(message = "Invalid email format")
    private String emailRecipient;

    private Boolean isActive = true;

    public BudgetDTO() {}

    public BudgetDTO(Long id, String name, BigDecimal monthlyThreshold, String currency,
                     String emailRecipient, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.monthlyThreshold = monthlyThreshold;
        this.currency = currency;
        this.emailRecipient = emailRecipient;
        this.isActive = isActive;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getMonthlyThreshold() { return monthlyThreshold; }
    public void setMonthlyThreshold(BigDecimal monthlyThreshold) { this.monthlyThreshold = monthlyThreshold; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getEmailRecipient() { return emailRecipient; }
    public void setEmailRecipient(String emailRecipient) { this.emailRecipient = emailRecipient; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String name;
        private BigDecimal monthlyThreshold;
        private String currency = "USD";
        private String emailRecipient;
        private Boolean isActive = true;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder monthlyThreshold(BigDecimal monthlyThreshold) { this.monthlyThreshold = monthlyThreshold; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder emailRecipient(String emailRecipient) { this.emailRecipient = emailRecipient; return this; }
        public Builder isActive(Boolean isActive) { this.isActive = isActive; return this; }

        public BudgetDTO build() {
            return new BudgetDTO(id, name, monthlyThreshold, currency, emailRecipient, isActive);
        }
    }
}
