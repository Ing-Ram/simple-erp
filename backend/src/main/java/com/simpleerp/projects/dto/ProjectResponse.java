package com.simpleerp.projects.dto;

import com.simpleerp.projects.BudgetHealth;
import com.simpleerp.projects.Project;
import com.simpleerp.projects.ProjectStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Project representation with derived budget figures; names and spend are resolved by the service. */
public record ProjectResponse(
        Long id,
        String name,
        Long customerId,
        String customerName,
        Long managerEmployeeId,
        String managerName,
        LocalDate startDate,
        LocalDate targetEndDate,
        BigDecimal budget,
        BigDecimal spent,
        double percentConsumed,
        BudgetHealth budgetHealth,
        String currency,
        ProjectStatus status) {

    /** Maps an entity plus the derived cost figures and cross-module names to the response. */
    public static ProjectResponse from(Project p, String customerName, String managerName,
                                       BigDecimal spent, double percentConsumed) {
        return new ProjectResponse(
                p.getId(),
                p.getName(),
                p.getCustomerId(),
                customerName,
                p.getManagerEmployeeId(),
                managerName,
                p.getStartDate(),
                p.getTargetEndDate(),
                p.getBudget().getAmount(),
                spent,
                percentConsumed,
                BudgetHealth.of(percentConsumed),
                p.getBudget().getCurrency(),
                p.getStatus());
    }
}
