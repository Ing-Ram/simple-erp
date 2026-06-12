package com.simpleerp.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Data access for AP bill payments. */
public interface BillPaymentRepository extends JpaRepository<BillPayment, Long> {

    /** Total amount already applied to the given bill. */
    @Query("select coalesce(sum(p.amount.amount), 0) from BillPayment p where p.bill.id = :billId")
    BigDecimal totalPaidForBill(@Param("billId") Long billId);

    /** Payments for one bill, most recent first, for the payment-history view. */
    List<BillPayment> findByBill_IdOrderByPaymentDateDescIdDesc(Long billId);

    /** Paid totals for a set of bills in one query — avoids N+1 when listing. */
    @Query("""
            select new com.simpleerp.finance.PaidTotal(p.bill.id, sum(p.amount.amount))
            from BillPayment p where p.bill.id in :billIds group by p.bill.id
            """)
    List<PaidTotal> paidTotals(@Param("billIds") Collection<Long> billIds);

    /** Total cash paid out on or after the given date — the "cash out" half of the dashboard panel. */
    @Query("select coalesce(sum(p.amount.amount), 0) from BillPayment p where p.paymentDate >= :since")
    BigDecimal cashOutSince(@Param("since") LocalDate since);
}
