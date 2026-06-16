package com.simpleerp.sales.dto;

import com.simpleerp.sales.MonthlyWon;
import com.simpleerp.sales.StageFunnel;
import java.math.BigDecimal;
import java.util.List;

/**
 * The complete Sales dashboard summary returned by {@code GET /api/v1/sales/dashboard} in one call.
 *
 * <p>{@code winRateLast90Days} is a ratio (0..1). Mirror this record field-for-field in types.ts.
 */
public record SalesDashboardResponse(
        BigDecimal openPipelineWeighted,
        BigDecimal wonThisQuarter,
        double winRateLast90Days,
        BigDecimal averageDealSize,
        List<StageFunnel> funnel,
        List<MonthlyWon> monthlyWon,
        List<NeedsAttentionRow> needsAttention) {
}
