package com.simpleerp.finance;

import com.simpleerp.finance.dto.FinanceDashboardResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Assembles the Finance dashboard summary from AR and AP aggregations.
 *
 * <p>Every figure is computed in SQL by the repositories; this service only stitches the
 * pieces together so the controller can return one {@link FinanceDashboardResponse}.
 */
@Service
@Transactional(readOnly = true)
public class FinanceDashboardService {

    /** Bills falling due within this many days count as "needs attention". */
    private static final int DUE_SOON_HORIZON_DAYS = 7;

    private final InvoiceRepository invoices;
    private final BillRepository bills;
    private final PaymentRepository payments;
    private final BillPaymentRepository billPayments;

    public FinanceDashboardService(InvoiceRepository invoices, BillRepository bills,
                                   PaymentRepository payments, BillPaymentRepository billPayments) {
        this.invoices = invoices;
        this.bills = bills;
        this.payments = payments;
        this.billPayments = billPayments;
    }

    /** Builds the full dashboard summary as of today. */
    public FinanceDashboardResponse summary() {
        LocalDate asOf = LocalDate.now();
        LocalDate since = asOf.minusDays(30);

        BigDecimal arOutstanding = invoices.totalOutstanding();
        BigDecimal apOutstanding = bills.totalOutstanding();

        List<OverdueDocument> overdueAr = invoices.overdueAsOf(asOf);
        BigDecimal overdueArAmount = overdueAr.stream()
                .map(OverdueDocument::outstanding)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<OverdueDocument> needsAttention = new ArrayList<>(overdueAr);
        needsAttention.addAll(bills.dueSoon(asOf, asOf.plusDays(DUE_SOON_HORIZON_DAYS)));
        needsAttention.sort(Comparator.comparing(OverdueDocument::dueDate));

        return new FinanceDashboardResponse(
                asOf,
                arOutstanding,
                apOutstanding,
                overdueAr.size(),
                overdueArAmount,
                arOutstanding.subtract(apOutstanding),
                payments.cashInSince(since),
                billPayments.cashOutSince(since),
                invoices.arAging(asOf),
                bills.apAging(asOf),
                needsAttention);
    }
}
