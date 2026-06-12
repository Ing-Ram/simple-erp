package com.simpleerp.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Data access for AP bills, including the dashboard aggregations. */
public interface BillRepository extends JpaRepository<Bill, Long> {

    /** AP aging buckets as of a date, mirroring the AR bucketing. */
    @Query(value = """
            select bucket, count(*) as documentCount, sum(outstanding) as outstanding
            from (
                select case
                           when b.due_date >= cast(:asOf as date) then 'CURRENT'
                           when b.due_date >= cast(:asOf as date) - 30 then 'D1_30'
                           when b.due_date >= cast(:asOf as date) - 60 then 'D31_60'
                           when b.due_date >= cast(:asOf as date) - 90 then 'D61_90'
                           else 'D90_PLUS'
                       end as bucket,
                       (select coalesce(sum(l.quantity * l.unit_price_amount), 0)
                          from bill_lines l where l.bill_id = b.id)
                     - (select coalesce(sum(p.amount_amount), 0)
                          from bill_payments p where p.bill_id = b.id) as outstanding
                from bills b
                where b.status in ('SENT', 'PARTIALLY_PAID')
            ) ap
            group by bucket
            """, nativeQuery = true)
    List<AgingBucket> apAging(@Param("asOf") LocalDate asOf);

    /** Total AP outstanding across all open bills: Σ(total − payments). */
    @Query(value = """
            select coalesce(sum(
                       (select coalesce(sum(l.quantity * l.unit_price_amount), 0)
                          from bill_lines l where l.bill_id = b.id)
                     - (select coalesce(sum(p.amount_amount), 0)
                          from bill_payments p where p.bill_id = b.id)), 0)
            from bills b
            where b.status in ('SENT', 'PARTIALLY_PAID')
            """, nativeQuery = true)
    BigDecimal totalOutstanding();

    /**
     * Open AP bills due between {@code asOf} and {@code until} (inclusive), soonest-due first, as
     * dashboard rows. The finance dashboard passes a 7-day window to flag bills "due soon". JPQL
     * (not native) so the {@code dueDate} maps straight to {@link LocalDate} in the projection.
     */
    @Query("""
            select new com.simpleerp.finance.OverdueDocument(
                       'AP', b.id, b.vendor.name, b.dueDate,
                       coalesce((select sum(l.quantity * l.unitPrice.amount)
                                   from BillLine l where l.bill = b), 0)
                       - coalesce((select sum(p.amount.amount)
                                   from BillPayment p where p.bill = b), 0))
            from Bill b
            where b.status in (com.simpleerp.finance.BillStatus.SENT,
                               com.simpleerp.finance.BillStatus.PARTIALLY_PAID)
              and b.dueDate >= :asOf and b.dueDate <= :until
            order by b.dueDate asc
            """)
    List<OverdueDocument> dueSoon(@Param("asOf") LocalDate asOf, @Param("until") LocalDate until);
}
