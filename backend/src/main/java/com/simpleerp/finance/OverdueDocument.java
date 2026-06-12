package com.simpleerp.finance;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A "needs attention" row for the finance dashboard: an overdue AR invoice or a soon-due AP bill.
 *
 * <p>{@code kind} is {@code "AR"} or {@code "AP"}; {@code party} is the customer or vendor name.
 */
public record OverdueDocument(
        String kind,
        Long documentId,
        String party,
        LocalDate dueDate,
        BigDecimal outstanding) {
}
