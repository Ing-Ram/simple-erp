package com.simpleerp.sales;

import com.simpleerp.shared.AuditableEntity;
import com.simpleerp.shared.Money;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/** A confirmed order, created by winning an opportunity (or directly for a repeat customer). */
@Entity
@Table(name = "sales_orders")
public class SalesOrder extends AuditableEntity {

    private Long customerId;
    private Long ownerEmployeeId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.OPEN;

    private java.time.LocalDate orderDate;

    /** The opportunity this order came from, if any. */
    private Long opportunityId;

    /** The AR invoice Finance created on invoicing; links Sales ↔ Finance both ways. */
    private Long invoiceId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesOrderLine> lines = new ArrayList<>();

    /** Sum of all line totals; zero (USD) for an order with no lines. */
    public Money total() {
        return lines.stream()
                .map(SalesOrderLine::lineTotal)
                .reduce(Money::plus)
                .orElse(Money.zero("USD"));
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

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public java.time.LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(java.time.LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public Long getOpportunityId() {
        return opportunityId;
    }

    public void setOpportunityId(Long opportunityId) {
        this.opportunityId = opportunityId;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public List<SalesOrderLine> getLines() {
        return lines;
    }
}
