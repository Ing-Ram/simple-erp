package com.simpleerp.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Data access for AR payments. */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /** Total amount already applied to the given invoice. */
    @Query("select coalesce(sum(p.amount.amount), 0) from Payment p where p.invoice.id = :invoiceId")
    BigDecimal totalPaidForInvoice(@Param("invoiceId") Long invoiceId);

    /** Payments for one invoice, most recent first, for the payment-history view. */
    List<Payment> findByInvoice_IdOrderByPaymentDateDescIdDesc(Long invoiceId);

    /** Paid totals for a set of invoices in one query — avoids N+1 when listing. */
    @Query("""
            select new com.simpleerp.finance.PaidTotal(p.invoice.id, sum(p.amount.amount))
            from Payment p where p.invoice.id in :invoiceIds group by p.invoice.id
            """)
    List<PaidTotal> paidTotals(@Param("invoiceIds") Collection<Long> invoiceIds);

    /** Total cash received on or after the given date — the "cash in" half of the dashboard panel. */
    @Query("select coalesce(sum(p.amount.amount), 0) from Payment p where p.paymentDate >= :since")
    BigDecimal cashInSince(@Param("since") LocalDate since);
}
