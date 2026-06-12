package com.simpleerp.finance.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Client payload for creating a draft AP bill. */
public record BillRequest(
        @NotNull Long vendorId,
        @NotNull LocalDate issueDate,
        @NotNull LocalDate dueDate,
        @NotEmpty List<@Valid LineItemRequest> lines) {

    /** One requested bill line. */
    public record LineItemRequest(
            @NotNull String description,
            @NotNull @Positive BigDecimal quantity,
            @NotNull @Positive BigDecimal unitPrice,
            @NotNull String currency) {
    }
}
