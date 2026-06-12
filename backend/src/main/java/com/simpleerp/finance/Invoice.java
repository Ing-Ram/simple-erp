package com.simpleerp.finance;

import com.simpleerp.shared.AuditableEntity;
import com.simpleerp.shared.Money;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** An accounts-receivable invoice: money a customer owes us. */
@Entity
@Table(name = "invoices")
public class Invoice extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private LocalDate issueDate;
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    /** Optional link back to the sales order this invoice was created from. */
    private Long salesOrderId;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceLine> lines = new ArrayList<>();

    /** Sum of all line totals; zero (USD) for an invoice with no lines. */
    public Money total() {
        return lines.stream()
                .map(InvoiceLine::lineTotal)
                .reduce(Money::plus)
                .orElse(Money.zero("USD"));
    }

    /** True when the invoice is unpaid past its due date as of the given day. */
    public boolean isOverdue(LocalDate asOf) {
        return (status == InvoiceStatus.SENT || status == InvoiceStatus.PARTIALLY_PAID)
                && dueDate.isBefore(asOf);
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public Long getSalesOrderId() {
        return salesOrderId;
    }

    public void setSalesOrderId(Long salesOrderId) {
        this.salesOrderId = salesOrderId;
    }

    public List<InvoiceLine> getLines() {
        return lines;
    }
}
