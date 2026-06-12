package com.simpleerp.finance.dto;

import com.simpleerp.finance.AgingBucket;
import com.simpleerp.finance.OverdueDocument;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * The complete Finance dashboard summary returned by {@code GET /api/v1/finance/dashboard}.
 *
 * <p>One call, fully aggregated server-side — the React dashboard renders this without doing
 * any math of its own. Mirror this record field-for-field in the frontend's types.ts.
 */
public record FinanceDashboardResponse(
        LocalDate asOf,
        BigDecimal arOutstanding,
        BigDecimal apOutstanding,
        long overdueArCount,
        BigDecimal overdueArAmount,
        BigDecimal netPosition,
        BigDecimal cashInLast30Days,
        BigDecimal cashOutLast30Days,
        List<AgingBucket> arAging,
        List<AgingBucket> apAging,
        List<OverdueDocument> needsAttention) {
}
