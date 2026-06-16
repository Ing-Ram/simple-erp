package com.simpleerp.hr.dto;

import com.simpleerp.hr.LeaveType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** Client payload for submitting a leave request. */
public record LeaveRequestRequest(
        @NotNull Long employeeId,
        @NotNull LeaveType type,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate) {
}
