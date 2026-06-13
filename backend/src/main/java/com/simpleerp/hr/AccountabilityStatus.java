package com.simpleerp.hr;

/**
 * Where each active employee stands in an emergency roll-call. Derived at read time, never stored.
 *
 * <p>Precedence favors physical reality: being in the building (PRESENT) is resolved before any
 * excused-absence status, so the muster captain never overlooks someone who is actually on site.
 */
public enum AccountabilityStatus {
    /** Checked in on site and not yet checked out — physically in the building. */
    PRESENT,
    /** Checked in on site earlier today but has since checked out. */
    CHECKED_OUT,
    /** Checked in as working remotely — accounted for, not in the building. */
    REMOTE,
    /** On approved leave covering today — excused. */
    ON_LEAVE,
    /** Active employee with no check-in and no leave today — status unknown, must be located. */
    UNACCOUNTED
}
