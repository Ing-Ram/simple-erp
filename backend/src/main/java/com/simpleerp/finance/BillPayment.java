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

/**
 * A payment made against exactly one AP bill; partial payments are allowed.
 *
 * <p>Kept separate from {@link Payment} (AR) rather than unified into one polymorphic table,
 * so each side's reports and lifecycles stay independent — see ap-ar.md.
 */
@Entity
@Table(name = "bill_payments")
public class BillPayment extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id")
    private Bill bill;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "amount_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "amount_currency"))})
    private Money amount;

    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    /** Bank routing number on the check; populated only for CHECK payments, null otherwise. */
    private String routingNumber;

    /**
     * Bank account number on the check, stored masked (all but the last 3 digits); populated only
     * for CHECK payments, null otherwise. The full number is never persisted.
     */
    private String accountNumber;

    /** Check number as written on the check; populated only for CHECK payments, null otherwise. */
    private String checkNumber;

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
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
