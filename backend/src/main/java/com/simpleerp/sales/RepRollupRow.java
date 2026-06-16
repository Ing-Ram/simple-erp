package com.simpleerp.sales;

import java.math.BigDecimal;

/**
 * Per-owner opportunity aggregates straight from SQL: won count and value, lost count, and open
 * weighted pipeline scaled by 100 (the service divides to get the real figure and derives win rate).
 */
public record RepRollupRow(
        Long ownerEmployeeId,
        long wonCount,
        BigDecimal wonValue,
        long lostCount,
        BigDecimal openWeightedTimes100) {
}
