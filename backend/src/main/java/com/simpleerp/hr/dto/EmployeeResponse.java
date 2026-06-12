package com.simpleerp.hr.dto;

import com.simpleerp.hr.Employee;
import com.simpleerp.hr.EmployeeStatus;
import java.time.LocalDate;

/**
 * Employee representation returned to clients. {@code status} is the effective status — ON_LEAVE is
 * derived from approved leave covering today, never stored. Salary is omitted here; expose it only
 * where reporting needs it.
 */
public record EmployeeResponse(
        Long id,
        String name,
        String email,
        Long departmentId,
        String departmentName,
        String position,
        LocalDate hireDate,
        LocalDate terminationDate,
        EmployeeStatus status) {

    /** Maps an entity to its response shape, given whether the employee is on leave as of today. */
    public static EmployeeResponse from(Employee e, boolean onLeaveToday) {
        return new EmployeeResponse(
                e.getId(),
                e.getName(),
                e.getEmail(),
                e.getDepartment() == null ? null : e.getDepartment().getId(),
                e.getDepartment() == null ? null : e.getDepartment().getName(),
                e.getPosition(),
                e.getHireDate(),
                e.getTerminationDate(),
                e.effectiveStatus(onLeaveToday));
    }
}
