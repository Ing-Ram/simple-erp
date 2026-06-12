package com.simpleerp.finance.dto;

import com.simpleerp.finance.Bill;
import com.simpleerp.finance.BillStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Bill representation returned to clients. Mirror this in the frontend's types.ts. */
public record BillResponse(
        Long id,
        Long vendorId,
        String vendorName,
        LocalDate issueDate,
        LocalDate dueDate,
        BillStatus status,
        boolean overdue,
        BigDecimal total,
        BigDecimal amountPaid,
        BigDecimal outstanding,
        String currency,
        List<LineResponse> lines) {

    /** One bill line as returned to clients. */
    public record LineResponse(Long id, String description, BigDecimal quantity, BigDecimal unitPrice) {
    }

    /**
     * Maps an entity to its response shape; the single home for this mapping.
     *
     * @param amountPaid payments applied so far, supplied by the service from the payment repository
     */
    public static BillResponse from(Bill bill, BigDecimal amountPaid, LocalDate asOf) {
        List<LineResponse> lines = bill.getLines().stream()
                .map(l -> new LineResponse(l.getId(), l.getDescription(), l.getQuantity(),
                        l.getUnitPrice().getAmount()))
                .toList();
        BigDecimal total = bill.total().getAmount();
        return new BillResponse(
                bill.getId(),
                bill.getVendor().getId(),
                bill.getVendor().getName(),
                bill.getIssueDate(),
                bill.getDueDate(),
                bill.getStatus(),
                bill.isOverdue(asOf),
                total,
                amountPaid,
                total.subtract(amountPaid),
                bill.total().getCurrency(),
                lines);
    }
}
