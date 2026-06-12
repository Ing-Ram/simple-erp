package com.simpleerp.hr;

import com.simpleerp.hr.dto.HrDashboardResponse;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Assembles the HR dashboard summary from employee and leave aggregations.
 *
 * <p>Every figure is computed in SQL by the repositories; this service only stitches them into one
 * {@link HrDashboardResponse}.
 */
@Service
@Transactional(readOnly = true)
public class HrDashboardService {

    /** "Who's out" looks at today plus this many days ahead. */
    private static final int WHOS_OUT_HORIZON_DAYS = 14;

    private final EmployeeRepository employees;
    private final LeaveRequestRepository leave;

    public HrDashboardService(EmployeeRepository employees, LeaveRequestRepository leave) {
        this.employees = employees;
        this.leave = leave;
    }

    /** Builds the full dashboard summary as of today. */
    public HrDashboardResponse summary() {
        LocalDate today = LocalDate.now();

        long activeHeadcount = employees.countByTerminationDateIsNull();
        long terminationsLast12Months = employees.countByTerminationDateGreaterThanEqual(today.minusDays(365));

        return new HrDashboardResponse(
                activeHeadcount,
                employees.countByHireDateGreaterThanEqual(today.minusDays(90)),
                leave.countByStatus(LeaveStatus.PENDING),
                turnoverRate(terminationsLast12Months, activeHeadcount),
                employees.headcountByDepartment(),
                leave.whosOut(today, today.plusDays(WHOS_OUT_HORIZON_DAYS)),
                leave.pendingOldestFirst());
    }

    /**
     * Terminations over average headcount as a ratio. Average headcount is approximated by current
     * active headcount (a documented v1 simplification); returns 0 when there is no headcount.
     */
    private double turnoverRate(long terminations, long activeHeadcount) {
        return activeHeadcount == 0 ? 0.0 : (double) terminations / activeHeadcount;
    }
}
