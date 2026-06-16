package com.simpleerp.hr.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Client payload for hiring an employee. */
public record EmployeeRequest(
        @NotNull String name,
        @Email String email,
        @NotNull Long departmentId,
        String position,
        @NotNull LocalDate hireDate,
        @NotNull @Positive BigDecimal salary,
        @NotNull String currency) {
}
