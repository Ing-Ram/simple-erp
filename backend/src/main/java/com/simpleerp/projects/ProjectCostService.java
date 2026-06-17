package com.simpleerp.projects;

import com.simpleerp.hr.EmployeeService;
import com.simpleerp.shared.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Computes actual project spend and budget health. Spend is Σ(hours × the assignee's HR hourly
 * cost) — never stored, always derived, and the salary it rests on stays in HR.
 */
@Service
@Transactional(readOnly = true)
public class ProjectCostService {

    private final TimeEntryRepository timeEntries;
    private final EmployeeService employees;

    public ProjectCostService(TimeEntryRepository timeEntries, EmployeeService employees) {
        this.timeEntries = timeEntries;
        this.employees = employees;
    }

    /** Total spent on a project: each employee's logged hours times their HR hourly cost. */
    public Money spent(Long projectId, String currency) {
        Money total = Money.zero(currency);
        for (EmployeeHours eh : timeEntries.hoursByEmployeeForProject(projectId)) {
            total = total.plus(employees.hourlyCost(eh.employeeId()).times(eh.hours()));
        }
        return total;
    }

    /** Spent ÷ budget as a ratio (0..n); 0 when there is no budget. */
    public double percentConsumed(Project project, Money spent) {
        BigDecimal budget = project.getBudget().getAmount();
        if (budget.signum() <= 0) {
            return 0.0;
        }
        return spent.getAmount().divide(budget, 4, RoundingMode.HALF_UP).doubleValue();
    }
}
