package com.cloudmonitor.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity representing daily cost data per AWS service.
 */
@Entity
@Table(name = "cost_data",
       uniqueConstraints = @UniqueConstraint(columnNames = {"date", "service_name"}),
       indexes = {
           @Index(name = "idx_cost_date", columnList = "date"),
           @Index(name = "idx_cost_service", columnList = "service_name")
       })
public class CostData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "service_name", nullable = false, length = 255)
    private String serviceName;

    @Column(name = "usage_type", length = 255)
    private String usageType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 6)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    public CostData() {}

    public CostData(Long id, LocalDate date, String serviceName, String usageType,
                    BigDecimal amount, String currency) {
        this.id = id;
        this.date = date;
        this.serviceName = serviceName;
        this.usageType = usageType;
        this.amount = amount;
        this.currency = currency;
    }

    public CostData(LocalDate date, String serviceName, String usageType,
                    BigDecimal amount, String currency) {
        this.date = date;
        this.serviceName = serviceName;
        this.usageType = usageType;
        this.amount = amount;
        this.currency = currency;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getUsageType() { return usageType; }
    public void setUsageType(String usageType) { this.usageType = usageType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CostData costData = (CostData) o;
        return Objects.equals(id, costData.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
