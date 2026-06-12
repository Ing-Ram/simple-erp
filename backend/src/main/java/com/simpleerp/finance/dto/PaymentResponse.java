package com.simpleerp.finance.dto;

import com.simpleerp.finance.Payment;
import com.simpleerp.finance.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One AR payment as returned to clients; check fields are null for non-CHECK methods.
 * {@code accountNumber} is already masked to its last 3 digits (that is all that is stored).
 */
public record PaymentResponse(
        Long id,
        BigDecimal amount,
        String currency,
        LocalDate paymentDate,
        PaymentMethod method,
        String routingNumber,
        String accountNumber,
        String checkNumber) {

    /** Maps an entity to its response shape; the single home for this mapping. */
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getAmount().getAmount(),
                p.getAmount().getCurrency(),
                p.getPaymentDate(),
                p.getMethod(),
                p.getRoutingNumber(),
                p.getAccountNumber(),
                p.getCheckNumber());
    }
}
