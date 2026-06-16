package com.simpleerp.sales;

import com.simpleerp.finance.CustomerService;
import com.simpleerp.sales.dto.NeedsAttentionRow;
import com.simpleerp.sales.dto.SalesDashboardResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Assembles the Sales dashboard summary from pipeline and order aggregations. */
@Service
@Transactional(readOnly = true)
public class SalesDashboardService {

    private static final List<OpportunityStage> OPEN_STAGES = List.of(
            OpportunityStage.PROSPECTING, OpportunityStage.QUALIFIED,
            OpportunityStage.PROPOSAL, OpportunityStage.NEGOTIATION);
    private static final int TRAILING_MONTHS = 6;

    private final OpportunityRepository opportunities;
    private final SalesOrderRepository orders;
    private final CustomerService customers;

    public SalesDashboardService(OpportunityRepository opportunities, SalesOrderRepository orders,
                                 CustomerService customers) {
        this.opportunities = opportunities;
        this.orders = orders;
        this.customers = customers;
    }

    /** Builds the full dashboard summary as of today. */
    public SalesDashboardResponse summary() {
        LocalDate today = LocalDate.now();

        BigDecimal weighted = opportunities.weightedPipelineTimes100().divide(BigDecimal.valueOf(100));
        BigDecimal wonThisQuarter = opportunities.wonAmountSince(quarterStart(today));

        long won90 = opportunities.countByStageAndClosedDateGreaterThanEqual(
                OpportunityStage.WON, today.minusDays(90));
        long lost90 = opportunities.countByStageAndClosedDateGreaterThanEqual(
                OpportunityStage.LOST, today.minusDays(90));
        double winRate = (won90 + lost90) == 0 ? 0.0 : (double) won90 / (won90 + lost90);

        long wonCount = opportunities.countByStage(OpportunityStage.WON);
        BigDecimal avgDeal = wonCount == 0
                ? BigDecimal.ZERO
                : opportunities.totalWonValue().divide(BigDecimal.valueOf(wonCount), 2, RoundingMode.HALF_UP);

        return new SalesDashboardResponse(
                weighted,
                wonThisQuarter,
                winRate,
                avgDeal,
                funnel(),
                monthlyWon(today),
                needsAttention(today));
    }

    /** The open stages in pipeline order, filling any stage that currently has no opportunities. */
    private List<StageFunnel> funnel() {
        Map<OpportunityStage, StageFunnel> byStage = new HashMap<>();
        for (StageFunnel row : opportunities.funnel()) {
            byStage.put(row.stage(), row);
        }
        return OPEN_STAGES.stream()
                .map(s -> byStage.getOrDefault(s, new StageFunnel(s, 0, BigDecimal.ZERO)))
                .toList();
    }

    /** Won revenue for each of the last {@code TRAILING_MONTHS} months, oldest first. */
    private List<MonthlyWon> monthlyWon(LocalDate today) {
        YearMonth current = YearMonth.from(today);
        YearMonth since = current.minusMonths(TRAILING_MONTHS - 1L);

        Map<YearMonth, BigDecimal> totals = new HashMap<>();
        for (WonAmount won : opportunities.wonSince(since.atDay(1))) {
            totals.merge(YearMonth.from(won.closedDate()), won.amount(), BigDecimal::add);
        }
        List<MonthlyWon> result = new ArrayList<>();
        for (int i = 0; i < TRAILING_MONTHS; i++) {
            YearMonth month = since.plusMonths(i);
            result.add(new MonthlyWon(month.toString(), totals.getOrDefault(month, BigDecimal.ZERO)));
        }
        return result;
    }

    /** Open opportunities past their close date and fulfilled-but-uninvoiced orders, soonest first. */
    private List<NeedsAttentionRow> needsAttention(LocalDate today) {
        List<NeedsAttentionRow> rows = new ArrayList<>();
        for (Opportunity o : opportunities.findByStageInAndExpectedCloseDateBefore(OPEN_STAGES, today)) {
            rows.add(new NeedsAttentionRow("OPPORTUNITY", o.getId(), customerName(o.getCustomerId()),
                    o.getExpectedValue().getAmount(), o.getExpectedCloseDate()));
        }
        for (SalesOrder order : orders.findByStatus(OrderStatus.FULFILLED)) {
            rows.add(new NeedsAttentionRow("ORDER", order.getId(), customerName(order.getCustomerId()),
                    order.total().getAmount(), order.getOrderDate()));
        }
        rows.sort((a, b) -> a.date().compareTo(b.date()));
        return rows;
    }

    private String customerName(Long customerId) {
        return customers.get(customerId).getName();
    }

    /** First day of the calendar quarter containing the given date. */
    private LocalDate quarterStart(LocalDate date) {
        int firstMonthOfQuarter = ((date.getMonthValue() - 1) / 3) * 3 + 1;
        return LocalDate.of(date.getYear(), firstMonthOfQuarter, 1);
    }
}
