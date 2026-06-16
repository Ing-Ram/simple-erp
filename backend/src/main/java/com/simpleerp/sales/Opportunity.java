package com.simpleerp.sales;

import com.simpleerp.shared.AuditableEntity;
import com.simpleerp.shared.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A deal in the pipeline. References its customer and owner by id only — Sales resolves them
 * through Finance's CustomerService and HR's EmployeeService, never their tables.
 */
@Entity
@Table(name = "opportunities")
public class Opportunity extends AuditableEntity {

    private Long customerId;
    private Long ownerEmployeeId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "expected_value_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "expected_value_currency"))})
    private Money expectedValue;

    /** Likelihood of winning, 0–100. */
    private int probability;

    private LocalDate expectedCloseDate;

    @Enumerated(EnumType.STRING)
    private OpportunityStage stage = OpportunityStage.PROSPECTING;

    /** The stage held before LOST, so a reopened opportunity returns to where it was. */
    @Enumerated(EnumType.STRING)
    private OpportunityStage previousStage;

    /** Set when the opportunity is won or lost; drives win-rate and "won this quarter". */
    private LocalDate closedDate;

    /** Required when LOST. */
    private String lostReason;

    /** The order created on winning; links Sales → the rest of fulfilment. */
    private Long salesOrderId;

    /** Probability-weighted value: expected value × probability ÷ 100. */
    public Money weightedValue() {
        return expectedValue.times(BigDecimal.valueOf(probability).movePointLeft(2));
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getOwnerEmployeeId() {
        return ownerEmployeeId;
    }

    public void setOwnerEmployeeId(Long ownerEmployeeId) {
        this.ownerEmployeeId = ownerEmployeeId;
    }

    public Money getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(Money expectedValue) {
        this.expectedValue = expectedValue;
    }

    public int getProbability() {
        return probability;
    }

    public void setProbability(int probability) {
        this.probability = probability;
    }

    public LocalDate getExpectedCloseDate() {
        return expectedCloseDate;
    }

    public void setExpectedCloseDate(LocalDate expectedCloseDate) {
        this.expectedCloseDate = expectedCloseDate;
    }

    public OpportunityStage getStage() {
        return stage;
    }

    public void setStage(OpportunityStage stage) {
        this.stage = stage;
    }

    public OpportunityStage getPreviousStage() {
        return previousStage;
    }

    public void setPreviousStage(OpportunityStage previousStage) {
        this.previousStage = previousStage;
    }

    public LocalDate getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(LocalDate closedDate) {
        this.closedDate = closedDate;
    }

    public String getLostReason() {
        return lostReason;
    }

    public void setLostReason(String lostReason) {
        this.lostReason = lostReason;
    }

    public Long getSalesOrderId() {
        return salesOrderId;
    }

    public void setSalesOrderId(Long salesOrderId) {
        this.salesOrderId = salesOrderId;
    }
}
