package com.simpleerp.projects.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * The complete Projects dashboard summary returned by {@code GET /api/v1/projects/dashboard}.
 *
 * <p>{@code utilization} is a ratio (0..1): hours logged in the last 30 days over the active
 * workforce's capacity. Mirror this record field-for-field in the frontend's types.ts.
 */
public record ProjectsDashboardResponse(
        long activeProjects,
        long atRiskOrOverBudget,
        BigDecimal hoursThisWeek,
        double utilization,
        List<ProjectBudgetRow> budgetVsActual,
        List<UpcomingMilestone> upcomingMilestones,
        List<ProjectAttentionRow> needsAttention) {
}
