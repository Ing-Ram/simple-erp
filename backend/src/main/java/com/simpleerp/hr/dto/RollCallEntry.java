package com.simpleerp.hr.dto;

import com.simpleerp.hr.AccountabilityStatus;
import java.time.Instant;

/**
 * One person on the roll-call: their accountability status and the moment it last changed
 * ({@code since} is the check-in or check-out time; null when there is no presence record today).
 */
public record RollCallEntry(
        Long employeeId,
        String name,
        String department,
        AccountabilityStatus status,
        Instant since) {
}
