package com.simpleerp.sales.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Client payload for creating an opportunity for an existing customer. */
public record OpportunityRequest(
        @NotNull Long customerId,
        @NotNull Long ownerEmployeeId,
        @NotNull @Positive BigDecimal expectedValue,
        @NotNull String currency,
        @Min(0) @Max(100) int probability,
        @NotNull LocalDate expectedCloseDate) {
}
