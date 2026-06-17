package com.simpleerp.projects.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Client payload for logging time against a task. Hours must be in (0, 24]. */
public record TimeEntryRequest(
        @NotNull Long taskId,
        @NotNull Long employeeId,
        @NotNull LocalDate entryDate,
        @NotNull @Positive @DecimalMax("24.0") BigDecimal hours,
        String note) {
}
