package com.simpleerp.sales;

/**
 * Lifecycle of a sales order.
 *
 * <pre>
 * OPEN → FULFILLED → INVOICED      (FULFILLED→INVOICED hands off to Finance)
 *   └──→ CANCELLED (only while OPEN)
 * </pre>
 */
public enum OrderStatus {
    OPEN, FULFILLED, INVOICED, CANCELLED
}
