package com.simpleerp.finance;

/**
 * Lifecycle states of an AP bill — the same shape as an AR invoice.
 *
 * <p>As with invoices, overdue is derived from {@code dueDate}, never stored.
 */
public enum BillStatus {
    DRAFT, SENT, PARTIALLY_PAID, PAID, VOID
}
