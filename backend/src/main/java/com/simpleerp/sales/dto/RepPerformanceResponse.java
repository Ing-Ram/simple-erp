package com.simpleerp.sales.dto;

import java.math.BigDecimal;

/**
 * One salesperson's deal performance. {@code winRate} is a ratio (0..1). Mirror field-for-field in
 * the frontend's types.ts.
 */
public record RepPerformanceResponse(
        Long employeeId,
        String name,
        long wonCount,
        BigDecimal wonValue,
        BigDecimal averageDealSize,
        long lostCount,
        double winRate,
        BigDecimal openPipelineWeighted) {
}
