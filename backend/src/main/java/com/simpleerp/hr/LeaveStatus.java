package com.simpleerp.hr;

/**
 * Lifecycle states of a leave request.
 *
 * <pre>
 * PENDING → APPROVED → CANCELLED (only while the leave is still in the future)
 *        └→ REJECTED
 * </pre>
 */
public enum LeaveStatus {
    PENDING, APPROVED, REJECTED, CANCELLED
}
