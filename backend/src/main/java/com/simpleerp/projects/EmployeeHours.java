package com.simpleerp.projects;

import java.math.BigDecimal;

/** Total hours one employee logged (on a project), used to compute actual cost via HR hourly cost. */
public record EmployeeHours(Long employeeId, BigDecimal hours) {
}
