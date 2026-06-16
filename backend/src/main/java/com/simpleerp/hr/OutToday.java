package com.simpleerp.hr;

import java.time.LocalDate;

/** One "who's out" row: an employee on approved leave within the dashboard window. */
public record OutToday(Long employeeId, String name, LeaveType leaveType, LocalDate startDate, LocalDate endDate) {
}
