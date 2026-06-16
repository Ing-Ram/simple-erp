package com.simpleerp.sales.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Client payload for qualifying a lead: it creates the customer (with these payment terms) and an
 * opportunity owned by the given employee.
 */
public record QualifyLeadRequest(
        @NotNull Long ownerEmployeeId,
        @NotNull @Positive BigDecimal expectedValue,
        @NotNull String currency,
        @Min(0) @Max(100) int probability,
        @NotNull LocalDate expectedCloseDate,
        @Min(0) int paymentTermsDays) {
}
