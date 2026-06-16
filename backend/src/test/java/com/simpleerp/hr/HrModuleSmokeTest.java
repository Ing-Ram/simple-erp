package com.simpleerp.hr;

import static org.assertj.core.api.Assertions.assertThat;

import com.simpleerp.hr.dto.DepartmentRequest;
import com.simpleerp.hr.dto.EmployeeRequest;
import com.simpleerp.hr.dto.LeaveRequestRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Boots the full context against H2 so the V5 HR migration applies, then drives the leave workflow
 * and dashboard aggregations end to end (JPQL projections included). {@code @Transactional} rolls
 * each method back so the shared in-memory DB stays isolated; the demo seed is excluded in tests.
 */
@SpringBootTest
@Transactional
class HrModuleSmokeTest {

    @Autowired private HrDashboardService dashboard;
    @Autowired private DepartmentService departments;
    @Autowired private EmployeeService employees;
    @Autowired private LeaveRequestService leave;

    @Test
    void migrationsApplyAndDashboardAggregatesOnEmptyData() {
        var summary = dashboard.summary();

        assertThat(summary.activeHeadcount()).isZero();
        assertThat(summary.turnoverRate()).isZero();
        assertThat(summary.headcountByDepartment()).isEmpty();
        assertThat(summary.whosOut()).isEmpty();
        assertThat(summary.needsAttention()).isEmpty();
    }

    @Test
    void leaveSubmittedThenApprovedSurfacesAcrossTheDashboard() {
        Long deptId = departments.create(new DepartmentRequest("Engineering", null)).id();
        Long empId = employees.create(new EmployeeRequest(
                "Alice Chen", "alice@simpleerp.example", deptId, "Engineer",
                LocalDate.now().minusDays(10), new BigDecimal("120000.00"), "USD")).id();

        // A recent hire shows up in headcount and the 90-day KPI.
        var afterHire = dashboard.summary();
        assertThat(afterHire.activeHeadcount()).isEqualTo(1);
        assertThat(afterHire.hiresLast90Days()).isEqualTo(1);
        assertThat(afterHire.headcountByDepartment()).hasSize(1);

        // A submitted request is pending; once approved and covering today, the person is "out".
        Long reqId = leave.submit(new LeaveRequestRequest(
                empId, com.simpleerp.hr.LeaveType.VACATION,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(3))).id();
        assertThat(dashboard.summary().pendingLeaveRequests()).isEqualTo(1);

        leave.approve(reqId, "HR");
        var afterApprove = dashboard.summary();
        assertThat(afterApprove.pendingLeaveRequests()).isZero();
        assertThat(afterApprove.whosOut()).hasSize(1);
        assertThat(employees.get(empId).status()).isEqualTo(EmployeeStatus.ON_LEAVE);
    }

    @Test
    void leaveRequestsListReturnsAllAndFiltersByStatus() {
        Long deptId = departments.create(new DepartmentRequest("Engineering", null)).id();
        Long empId = employees.create(new EmployeeRequest(
                "Bob Martinez", "bob@simpleerp.example", deptId, "Engineer",
                LocalDate.now().minusDays(30), new BigDecimal("110000.00"), "USD")).id();
        Long reqId = leave.submit(new LeaveRequestRequest(
                empId, com.simpleerp.hr.LeaveType.SICK,
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(6))).id();

        // The submitted request appears in the full list and the PENDING filter, not in APPROVED.
        assertThat(leave.list(null)).extracting("id").containsExactly(reqId);
        assertThat(leave.list(LeaveStatus.PENDING)).extracting("id").containsExactly(reqId);
        assertThat(leave.list(LeaveStatus.APPROVED)).isEmpty();
    }
}
