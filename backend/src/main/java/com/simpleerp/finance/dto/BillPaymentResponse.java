package com.simpleerp.finance.dto;

import com.simpleerp.finance.BillPayment;
import com.simpleerp.finance.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One AP bill payment as returned to clients; check fields are null for non-CHECK methods.
 * {@code accountNumber} is already masked to its last 3 digits (that is all that is stored).
 */
public record BillPaymentResponse(
        Long id,
        BigDecimal amount,
        String currency,
        LocalDate paymentDate,
        PaymentMethod method,
        String routingNumber,
        String accountNumber,
        String checkNumber) {

    /** Maps an entity to its response shape; the single home for this mapping. */
    public static BillPaymentResponse from(BillPayment p) {
        return new BillPaymentResponse(
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
