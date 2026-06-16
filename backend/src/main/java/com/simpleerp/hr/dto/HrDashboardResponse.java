package com.simpleerp.hr.dto;

import com.simpleerp.hr.DepartmentHeadcount;
import com.simpleerp.hr.OutToday;
import com.simpleerp.hr.PendingLeave;
import java.util.List;

/**
 * The complete HR dashboard summary returned by {@code GET /api/v1/hr/dashboard} in one call.
 *
 * <p>{@code turnoverRate} is a ratio (0..1): terminations in the last 12 months over average
 * headcount. Mirror this record field-for-field in the frontend's types.ts.
 */
public record HrDashboardResponse(
        long activeHeadcount,
        long hiresLast90Days,
        long pendingLeaveRequests,
        double turnoverRate,
        List<DepartmentHeadcount> headcountByDepartment,
        List<OutToday> whosOut,
        List<PendingLeave> needsAttention) {
}
