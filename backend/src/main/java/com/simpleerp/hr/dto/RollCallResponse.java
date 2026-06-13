package com.simpleerp.hr.dto;

import java.time.Instant;
import java.util.List;

/**
 * The emergency roll-call: every active employee reconciled into an accountability status as of
 * {@code asOf}, plus per-status counts so the muster captain sees the headline numbers at a glance.
 */
public record RollCallResponse(
        Instant asOf,
        long presentCount,
        long checkedOutCount,
        long remoteCount,
        long onLeaveCount,
        long unaccountedCount,
        List<RollCallEntry> entries) {
}
