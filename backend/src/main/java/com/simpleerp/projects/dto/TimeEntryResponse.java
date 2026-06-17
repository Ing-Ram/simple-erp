package com.simpleerp.projects.dto;

import com.simpleerp.projects.TimeEntry;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Time entry representation; the employee name is resolved by the service. */
public record TimeEntryResponse(
        Long id,
        Long taskId,
        String taskTitle,
        Long employeeId,
        String employeeName,
        LocalDate entryDate,
        BigDecimal hours,
        String note) {

    /** Maps an entity to its response shape, given the resolved employee name. */
    public static TimeEntryResponse from(TimeEntry t, String employeeName) {
        return new TimeEntryResponse(
                t.getId(),
                t.getTask().getId(),
                t.getTask().getTitle(),
                t.getEmployeeId(),
                employeeName,
                t.getEntryDate(),
                t.getHours(),
                t.getNote());
    }
}
