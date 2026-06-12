package com.simpleerp.finance.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Client payload for creating a draft invoice. */
public record InvoiceRequest(
        @NotNull Long customerId,
        @NotNull LocalDate issueDate,
        @NotNull LocalDate dueDate,
        @NotEmpty List<@Valid LineItemRequest> lines) {

    /** One requested invoice line. */
    public record LineItemRequest(
            @NotNull String description,
            @NotNull @Positive BigDecimal quantity,
            @NotNull @Positive BigDecimal unitPrice,
            @NotNull String currency) {
    }
}
