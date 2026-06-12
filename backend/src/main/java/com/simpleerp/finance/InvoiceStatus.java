package com.simpleerp.finance;

/**
 * Lifecycle states of an AR invoice.
 *
 * <p>An overdue marker is intentionally absent here: it is derived from {@code dueDate} versus
 * the as-of date, never stored, so it can never go stale.
 */
public enum InvoiceStatus {
    DRAFT, SENT, PARTIALLY_PAID, PAID, VOID
}
