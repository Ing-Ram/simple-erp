package com.simpleerp.sales.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One "money sitting still" row for the dashboard: {@code kind} is {@code "OPPORTUNITY"} (open and
 * past its expected close date) or {@code "ORDER"} (fulfilled but not yet invoiced).
 */
public record NeedsAttentionRow(
        String kind,
        Long id,
        String customerName,
        BigDecimal amount,
        LocalDate date) {
}
