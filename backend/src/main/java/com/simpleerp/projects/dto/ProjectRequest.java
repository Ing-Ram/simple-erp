package com.simpleerp.projects.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Client payload for creating a project. {@code customerId} is null for internal projects. */
public record ProjectRequest(
        @NotNull String name,
        Long customerId,
        @NotNull Long managerEmployeeId,
        LocalDate startDate,
        LocalDate targetEndDate,
        @NotNull @PositiveOrZero BigDecimal budget,
        @NotNull String currency) {
}
