package com.simpleerp.projects;

import com.simpleerp.hr.EmployeeService;
import com.simpleerp.projects.dto.ProjectAttentionRow;
import com.simpleerp.projects.dto.ProjectBudgetRow;
import com.simpleerp.projects.dto.ProjectsDashboardResponse;
import com.simpleerp.projects.dto.UpcomingMilestone;
import com.simpleerp.shared.Money;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Assembles the Projects dashboard: budget vs. actual, utilization, milestones, needs-attention. */
@Service
@Transactional(readOnly = true)
public class ProjectsDashboardService {

    private static final int MILESTONE_HORIZON_DAYS = 30;
    /** Approximate per-employee capacity over ~30 days: 40 hours × 4 weeks. */
    private static final BigDecimal MONTHLY_CAPACITY_HOURS = BigDecimal.valueOf(160);

    private final ProjectRepository projects;
    private final TaskRepository tasks;
    private final MilestoneRepository milestones;
    private final TimeEntryRepository timeEntries;
    private final ProjectCostService cost;
    private final EmployeeService employees;

    public ProjectsDashboardService(ProjectRepository projects, TaskRepository tasks,
                                    MilestoneRepository milestones, TimeEntryRepository timeEntries,
                                    ProjectCostService cost, EmployeeService employees) {
        this.projects = projects;
        this.tasks = tasks;
        this.milestones = milestones;
        this.timeEntries = timeEntries;
        this.cost = cost;
        this.employees = employees;
    }

    /** Builds the full dashboard summary as of today. */
    public ProjectsDashboardResponse summary() {
        LocalDate today = LocalDate.now();

        List<ProjectBudgetRow> budgetVsActual = budgetRows();
        long atRiskOrOver = budgetVsActual.stream()
                .filter(r -> r.health() != BudgetHealth.ON_TRACK)
                .count();

        return new ProjectsDashboardResponse(
                projects.countByStatus(ProjectStatus.ACTIVE),
                atRiskOrOver,
                timeEntries.hoursLoggedSince(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))),
                utilization(today),
                budgetVsActual,
                upcomingMilestones(today),
                needsAttention(today));
    }

    /** One budget-vs-actual row per active project, worst (highest % consumed) first. */
    private List<ProjectBudgetRow> budgetRows() {
        List<ProjectBudgetRow> rows = new ArrayList<>();
        for (Project p : projects.findByStatus(ProjectStatus.ACTIVE)) {
            Money spent = cost.spent(p.getId(), p.getBudget().getCurrency());
            double percent = cost.percentConsumed(p, spent);
            rows.add(new ProjectBudgetRow(p.getId(), p.getName(), p.getBudget().getAmount(),
                    spent.getAmount(), percent, BudgetHealth.of(percent)));
        }
        rows.sort(Comparator.comparingDouble(ProjectBudgetRow::percentConsumed).reversed());
        return rows;
    }

    /** Hours logged in the last 30 days over the active workforce's capacity. */
    private double utilization(LocalDate today) {
        BigDecimal logged = timeEntries.hoursLoggedSince(today.minusDays(30));
        BigDecimal capacity = MONTHLY_CAPACITY_HOURS.multiply(BigDecimal.valueOf(employees.activeHeadcount()));
        if (capacity.signum() <= 0) {
            return 0.0;
        }
        return logged.doubleValue() / capacity.doubleValue();
    }

    /** Unresolved milestones due within the horizon, soonest first, overdue ones flagged. */
    private List<UpcomingMilestone> upcomingMilestones(LocalDate today) {
        return milestones.upcomingUnresolved(today.plusDays(MILESTONE_HORIZON_DAYS)).stream()
                .map(m -> new UpcomingMilestone(m.getId(), m.getProject().getName(), m.getName(),
                        m.getDueDate(), m.getDueDate() != null && m.getDueDate().isBefore(today)))
                .toList();
    }

    /** Overdue tasks (not done) and projects past their target end date that aren't completed. */
    private List<ProjectAttentionRow> needsAttention(LocalDate today) {
        List<ProjectAttentionRow> rows = new ArrayList<>();
        for (Task t : tasks.findByStatusNotAndDueDateBefore(TaskStatus.DONE, today)) {
            rows.add(new ProjectAttentionRow("TASK", t.getId(), t.getTitle(),
                    t.getProject().getName(), t.getDueDate()));
        }
        for (Project p : projects.findByStatusNotInAndTargetEndDateBefore(
                List.of(ProjectStatus.COMPLETED, ProjectStatus.CANCELLED), today)) {
            rows.add(new ProjectAttentionRow("PROJECT", p.getId(), p.getName(),
                    p.getName(), p.getTargetEndDate()));
        }
        rows.sort(Comparator.comparing(ProjectAttentionRow::dueDate,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return rows;
    }
}
