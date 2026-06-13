package com.simpleerp.hr.dto;

import com.simpleerp.hr.WorkMode;
import jakarta.validation.constraints.NotNull;

/** Client payload for checking an employee in for the day. */
public record CheckInRequest(@NotNull Long employeeId, @NotNull WorkMode workMode) {
}
