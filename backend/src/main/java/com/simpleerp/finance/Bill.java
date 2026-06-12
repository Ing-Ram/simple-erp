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

/** An accounts-payable bill: money we owe a vendor. Mirror of {@link Invoice} on the AP side. */
@Entity
@Table(name = "bills")
public class Bill extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    private LocalDate issueDate;
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private BillStatus status = BillStatus.DRAFT;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BillLine> lines = new ArrayList<>();

    /** Sum of all line totals; zero (USD) for a bill with no lines. */
    public Money total() {
        return lines.stream()
                .map(BillLine::lineTotal)
                .reduce(Money::plus)
                .orElse(Money.zero("USD"));
    }

    /** True when the bill is unpaid past its due date as of the given day. */
    public boolean isOverdue(LocalDate asOf) {
        return (status == BillStatus.SENT || status == BillStatus.PARTIALLY_PAID)
                && dueDate.isBefore(asOf);
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
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

    public BillStatus getStatus() {
        return status;
    }

    public void setStatus(BillStatus status) {
        this.status = status;
    }

    public List<BillLine> getLines() {
        return lines;
    }
}
