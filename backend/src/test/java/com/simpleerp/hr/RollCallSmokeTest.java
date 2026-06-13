package com.simpleerp.hr;

import static org.assertj.core.api.Assertions.assertThat;

import com.simpleerp.hr.dto.CheckInRequest;
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
 * Exercises the roll-call reconciliation against H2 with a realistic mix: someone present, someone
 * checked out, someone remote, someone on leave, and someone unaccounted.
 */
@SpringBootTest
@Transactional
class RollCallSmokeTest {

    @Autowired private DepartmentService departments;
    @Autowired private EmployeeService employees;
    @Autowired private LeaveRequestService leave;
    @Autowired private PresenceService presence;

    @Test
    void rollCallReconcilesEveryActiveEmployee() {
        Long dept = departments.create(new DepartmentRequest("Engineering", null)).id();
        Long present = hire(dept, "Present Pat");
        Long checkedOut = hire(dept, "Gone Gail");
        Long remote = hire(dept, "Remote Ravi");
        Long onLeave = hire(dept, "Away Ada");
        hire(dept, "Missing Mo"); // never checks in, not on leave → UNACCOUNTED

        presence.checkIn(new CheckInRequest(present, WorkMode.ON_SITE));
        presence.checkIn(new CheckInRequest(checkedOut, WorkMode.ON_SITE));
        presence.checkOut(checkedOut);
        presence.checkIn(new CheckInRequest(remote, WorkMode.REMOTE));

        Long leaveId = leave.submit(new LeaveRequestRequest(
                onLeave, LeaveType.VACATION, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1))).id();
        leave.approve(leaveId, "HR");

        var roll = presence.rollCall();

        assertThat(roll.presentCount()).isEqualTo(1);
        assertThat(roll.checkedOutCount()).isEqualTo(1);
        assertThat(roll.remoteCount()).isEqualTo(1);
        assertThat(roll.onLeaveCount()).isEqualTo(1);
        assertThat(roll.unaccountedCount()).isEqualTo(1);
        assertThat(roll.entries()).hasSize(5);
    }

    private Long hire(Long deptId, String name) {
        return employees.create(new EmployeeRequest(
                name, null, deptId, "Engineer", LocalDate.now().minusMonths(6),
                new BigDecimal("100000.00"), "USD")).id();
    }
}
