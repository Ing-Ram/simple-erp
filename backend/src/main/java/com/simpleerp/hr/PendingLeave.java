package com.simpleerp.hr;

import java.time.Instant;
import java.time.LocalDate;

/** One pending leave request row for the dashboard's needs-attention table. */
public record PendingLeave(
        Long requestId,
        Long employeeId,
        String name,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate,
        Instant requestedOn) {
}
