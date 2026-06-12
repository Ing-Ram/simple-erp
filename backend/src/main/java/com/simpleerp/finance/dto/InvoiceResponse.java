package com.simpleerp.finance.dto;

import com.simpleerp.finance.Invoice;
import com.simpleerp.finance.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Invoice representation returned to clients. Mirror this in the frontend's types.ts. */
public record InvoiceResponse(
        Long id,
        Long customerId,
        String customerName,
        LocalDate issueDate,
        LocalDate dueDate,
        InvoiceStatus status,
        boolean overdue,
        BigDecimal total,
        BigDecimal amountPaid,
        BigDecimal outstanding,
        String currency,
        List<LineResponse> lines) {

    /** One invoice line as returned to clients. */
    public record LineResponse(Long id, String description, BigDecimal quantity, BigDecimal unitPrice) {
    }

    /**
     * Maps an entity to its response shape; the single home for this mapping.
     *
     * @param amountPaid payments applied so far, supplied by the service from the payment repository
     */
    public static InvoiceResponse from(Invoice invoice, BigDecimal amountPaid, LocalDate asOf) {
        List<LineResponse> lines = invoice.getLines().stream()
                .map(l -> new LineResponse(l.getId(), l.getDescription(), l.getQuantity(),
                        l.getUnitPrice().getAmount()))
                .toList();
        BigDecimal total = invoice.total().getAmount();
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getCustomer().getId(),
                invoice.getCustomer().getName(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getStatus(),
                invoice.isOverdue(asOf),
                total,
                amountPaid,
                total.subtract(amountPaid),
                invoice.total().getCurrency(),
                lines);
    }
}
