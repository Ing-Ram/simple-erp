package com.simpleerp.projects.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Client payload for creating a task. {@code assigneeEmployeeId} is null when unassigned. */
public record TaskRequest(
        @NotNull Long projectId,
        @NotNull String title,
        Long assigneeEmployeeId,
        LocalDate dueDate,
        @PositiveOrZero BigDecimal estimateHours) {
}
