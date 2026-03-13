package com.cloudmonitor.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a budget threshold for cost alerting.
 */
@Entity
@Table(name = "budget")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "monthly_threshold", nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyThreshold;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "email_recipient", length = 255)
    private String emailRecipient;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "last_alert_sent")
    private LocalDateTime lastAlertSent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Budget() {}

    public Budget(Long id, String name, BigDecimal monthlyThreshold, String currency,
                  String emailRecipient, Boolean isActive, LocalDateTime lastAlertSent,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.monthlyThreshold = monthlyThreshold;
        this.currency = currency;
        this.emailRecipient = emailRecipient;
        this.isActive = isActive;
        this.lastAlertSent = lastAlertSent;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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

    public LocalDateTime getLastAlertSent() { return lastAlertSent; }
    public void setLastAlertSent(LocalDateTime lastAlertSent) { this.lastAlertSent = lastAlertSent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Budget budget = (Budget) o;
        return Objects.equals(id, budget.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
