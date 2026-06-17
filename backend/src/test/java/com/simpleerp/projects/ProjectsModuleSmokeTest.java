package com.simpleerp.projects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.simpleerp.hr.DepartmentService;
import com.simpleerp.hr.EmployeeService;
import com.simpleerp.hr.dto.DepartmentRequest;
import com.simpleerp.hr.dto.EmployeeRequest;
import com.simpleerp.projects.dto.MilestoneRequest;
import com.simpleerp.projects.dto.ProjectRequest;
import com.simpleerp.projects.dto.TaskRequest;
import com.simpleerp.projects.dto.TimeEntryRequest;
import com.simpleerp.shared.InvalidStateException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Boots the full context against H2 so the V9 migration applies, then exercises the Projects rules
 * end to end: HR-derived hourly cost, budget health, the completion-needs-milestones rule, the
 * time-lock on closed projects, and the dashboard aggregation.
 */
@SpringBootTest
@Transactional
class ProjectsModuleSmokeTest {

    @Autowired private DepartmentService departments;
    @Autowired private EmployeeService employees;
    @Autowired private ProjectService projects;
    @Autowired private TaskService tasks;
    @Autowired private MilestoneService milestones;
    @Autowired private TimeEntryService timeEntries;
    @Autowired private ProjectsDashboardService dashboard;

    @Test
    void hourlyCostIsSalaryOver2080() {
        Long emp = employee("Al", "104000.00");   // 104000 / 2080 = exactly 50.00
        assertThat(employees.hourlyCost(emp).getAmount()).isEqualByComparingTo("50.00");
    }

    @Test
    void budgetHealthTracksLoggedTimeAndCost() {
        Long emp = employee("Al", "104000.00");    // $50/hr
        Long project = activeProject(emp, "1000.00");
        Long task = tasks.create(new TaskRequest(project, "Build", emp, LocalDate.now().plusDays(5),
                new BigDecimal("40"))).id();

        log(task, emp, "10", LocalDate.now().minusDays(2));   // $500 = 50%
        assertThat(projects.get(project).budgetHealth()).isEqualTo(BudgetHealth.ON_TRACK);
        assertThat(projects.get(project).spent()).isEqualByComparingTo("500.00");

        log(task, emp, "8", LocalDate.now().minusDays(1));    // +$400 → $900 = 90%
        assertThat(projects.get(project).budgetHealth()).isEqualTo(BudgetHealth.AT_RISK);

        log(task, emp, "6", LocalDate.now());                 // +$300 → $1200 = 120%
        assertThat(projects.get(project).budgetHealth()).isEqualTo(BudgetHealth.OVER);
    }

    @Test
    void completingRequiresEveryMilestoneResolved() {
        Long emp = employee("Al", "104000.00");
        Long project = activeProject(emp, "1000.00");
        Long milestone = milestones.create(new MilestoneRequest(project, "Sign-off", LocalDate.now())).id();

        assertThatThrownBy(() -> projects.complete(project)).isInstanceOf(InvalidStateException.class);

        milestones.waive(milestone);
        assertThat(projects.complete(project).status()).isEqualTo(ProjectStatus.COMPLETED);
    }

    @Test
    void timeLocksOnceProjectIsClosed() {
        Long emp = employee("Al", "104000.00");
        Long project = activeProject(emp, "1000.00");
        Long task = tasks.create(new TaskRequest(project, "Build", emp, null, null)).id();
        projects.complete(project);   // no milestones → completes

        assertThatThrownBy(() -> log(task, emp, "4", LocalDate.now()))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void dashboardSummarizesActiveProjects() {
        Long emp = employee("Al", "104000.00");
        Long project = activeProject(emp, "1000.00");
        Long task = tasks.create(new TaskRequest(project, "Build", emp, null, null)).id();
        log(task, emp, "16", LocalDate.now());   // $800 = 80% → AT_RISK

        var summary = dashboard.summary();
        assertThat(summary.activeProjects()).isEqualTo(1);
        assertThat(summary.atRiskOrOverBudget()).isEqualTo(1);
        assertThat(summary.budgetVsActual()).hasSize(1);
        assertThat(summary.hoursThisWeek()).isNotNull();
    }

    private Long employee(String name, String salary) {
        Long dept = departments.create(new DepartmentRequest("Engineering", null)).id();
        return employees.create(new EmployeeRequest(
                name, name + "@simpleerp.example", dept, "Engineer",
                LocalDate.now().minusMonths(6), new BigDecimal(salary), "USD")).id();
    }

    private Long activeProject(Long managerId, String budget) {
        Long id = projects.create(new ProjectRequest(
                "Project", null, managerId, LocalDate.now(), LocalDate.now().plusMonths(2),
                new BigDecimal(budget), "USD")).id();
        projects.activate(id);
        return id;
    }

    private void log(Long taskId, Long empId, String hours, LocalDate date) {
        timeEntries.log(new TimeEntryRequest(taskId, empId, date, new BigDecimal(hours), null));
    }
}
