package com.simpleerp.finance.dto;

import com.simpleerp.finance.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Client payload for recording a payment against an invoice.
 *
 * <p>{@code routingNumber}, {@code accountNumber}, and {@code checkNumber} are optional and only
 * meaningful for CHECK payments. The account number is supplied in full; the service masks it to
 * its last 3 digits before storing, so the full value is never persisted.
 */
public record PaymentRequest(
        @NotNull @Positive BigDecimal amount,
        @NotNull String currency,
        @NotNull LocalDate paymentDate,
        @NotNull PaymentMethod method,
        String routingNumber,
        String accountNumber,
        String checkNumber) {
}
