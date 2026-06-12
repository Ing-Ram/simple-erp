package com.simpleerp.finance;

import com.simpleerp.shared.AuditableEntity;
import com.simpleerp.shared.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

/** A payment received against exactly one AR invoice; partial payments are allowed. */
@Entity
@Table(name = "payments")
public class Payment extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "amount_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "amount_currency"))})
    private Money amount;

    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    /** Payer's bank routing number; populated only for CHECK payments, null otherwise. */
    private String routingNumber;

    /**
     * Payer's bank account number, stored masked (all but the last 3 digits); populated only for
     * CHECK payments, null otherwise. The full number is never persisted.
     */
    private String accountNumber;

    /** Check number as written on the check; populated only for CHECK payments, null otherwise. */
    private String checkNumber;

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Money getAmount() {
        return amount;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }
}
