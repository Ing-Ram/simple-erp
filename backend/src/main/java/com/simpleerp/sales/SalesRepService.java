package com.simpleerp.sales;

import com.simpleerp.hr.EmployeeService;
import com.simpleerp.sales.dto.RepPerformanceResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Per-salesperson deal performance, rolled up from opportunities and named via HR. */
@Service
@Transactional(readOnly = true)
public class SalesRepService {

    private final OpportunityRepository opportunities;
    private final EmployeeService employees;

    public SalesRepService(OpportunityRepository opportunities, EmployeeService employees) {
        this.opportunities = opportunities;
        this.employees = employees;
    }

    /**
     * Each salesperson with at least one opportunity, ranked by won value descending. Won/lost
     * figures count deals closed on or after {@code since} (null = all time); open pipeline is
     * always the current snapshot.
     */
    public List<RepPerformanceResponse> performance(LocalDate since) {
        return opportunities.repRollup(since).stream()
                .map(this::toResponse)
                .sorted(Comparator.comparing(RepPerformanceResponse::wonValue).reversed())
                .toList();
    }

    private RepPerformanceResponse toResponse(RepRollupRow row) {
        long decided = row.wonCount() + row.lostCount();
        double winRate = decided == 0 ? 0.0 : (double) row.wonCount() / decided;
        BigDecimal avgDeal = row.wonCount() == 0
                ? BigDecimal.ZERO
                : row.wonValue().divide(BigDecimal.valueOf(row.wonCount()), 2, RoundingMode.HALF_UP);
        BigDecimal openWeighted = row.openWeightedTimes100().divide(BigDecimal.valueOf(100));
        return new RepPerformanceResponse(
                row.ownerEmployeeId(),
                employees.get(row.ownerEmployeeId()).name(),
                row.wonCount(),
                row.wonValue(),
                avgDeal,
                row.lostCount(),
                winRate,
                openWeighted);
    }
}
