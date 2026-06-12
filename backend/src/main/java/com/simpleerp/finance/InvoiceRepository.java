package com.simpleerp.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Data access for AR invoices, including the dashboard aggregations. */
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /**
     * AR aging buckets as of a date: Current, 1-30, 31-60, 61-90, 90+ days past due.
     * Outstanding = invoice total minus payments applied; computed in SQL so the dashboard
     * never aggregates in Java or React. Only open (SENT / PARTIALLY_PAID) invoices count.
     */
    @Query(value = """
            select bucket, count(*) as documentCount, sum(outstanding) as outstanding
            from (
                select case
                           when i.due_date >= cast(:asOf as date) then 'CURRENT'
                           when i.due_date >= cast(:asOf as date) - 30 then 'D1_30'
                           when i.due_date >= cast(:asOf as date) - 60 then 'D31_60'
                           when i.due_date >= cast(:asOf as date) - 90 then 'D61_90'
                           else 'D90_PLUS'
                       end as bucket,
                       (select coalesce(sum(l.quantity * l.unit_price_amount), 0)
                          from invoice_lines l where l.invoice_id = i.id)
                     - (select coalesce(sum(p.amount_amount), 0)
                          from payments p where p.invoice_id = i.id) as outstanding
                from invoices i
                where i.status in ('SENT', 'PARTIALLY_PAID')
            ) ar
            group by bucket
            """, nativeQuery = true)
    List<AgingBucket> arAging(@Param("asOf") LocalDate asOf);

    /** Total AR outstanding across all open invoices: Σ(total − payments). */
    @Query(value = """
            select coalesce(sum(
                       (select coalesce(sum(l.quantity * l.unit_price_amount), 0)
                          from invoice_lines l where l.invoice_id = i.id)
                     - (select coalesce(sum(p.amount_amount), 0)
                          from payments p where p.invoice_id = i.id)), 0)
            from invoices i
            where i.status in ('SENT', 'PARTIALLY_PAID')
            """, nativeQuery = true)
    BigDecimal totalOutstanding();

    /**
     * Overdue AR invoices as of a date, soonest-due first, as dashboard rows.
     * An invoice is overdue when it is open and its due date has passed. JPQL (not native) so the
     * {@code dueDate} maps straight to {@link LocalDate} in the projection.
     */
    @Query("""
            select new com.simpleerp.finance.OverdueDocument(
                       'AR', i.id, i.customer.name, i.dueDate,
                       coalesce((select sum(l.quantity * l.unitPrice.amount)
                                   from InvoiceLine l where l.invoice = i), 0)
                       - coalesce((select sum(p.amount.amount)
                                   from Payment p where p.invoice = i), 0))
            from Invoice i
            where i.status in (com.simpleerp.finance.InvoiceStatus.SENT,
                               com.simpleerp.finance.InvoiceStatus.PARTIALLY_PAID)
              and i.dueDate < :asOf
            order by i.dueDate asc
            """)
    List<OverdueDocument> overdueAsOf(@Param("asOf") LocalDate asOf);
}
