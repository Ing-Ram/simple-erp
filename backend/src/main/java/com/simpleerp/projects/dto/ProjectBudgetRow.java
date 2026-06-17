package com.simpleerp.projects.dto;

import com.simpleerp.projects.BudgetHealth;
import java.math.BigDecimal;

/** Budget vs. actual for one active project on the dashboard chart. */
public record ProjectBudgetRow(
        Long projectId,
        String name,
        BigDecimal budget,
        BigDecimal spent,
        double percentConsumed,
        BudgetHealth health) {
}
