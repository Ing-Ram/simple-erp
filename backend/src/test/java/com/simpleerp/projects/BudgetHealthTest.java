package com.simpleerp.projects;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Unit tests for the derived budget-health thresholds. */
class BudgetHealthTest {

    @Test
    void classifiesByPercentConsumed() {
        assertThat(BudgetHealth.of(0.0)).isEqualTo(BudgetHealth.ON_TRACK);
        assertThat(BudgetHealth.of(0.79)).isEqualTo(BudgetHealth.ON_TRACK);
        assertThat(BudgetHealth.of(0.80)).isEqualTo(BudgetHealth.AT_RISK);
        assertThat(BudgetHealth.of(1.00)).isEqualTo(BudgetHealth.AT_RISK);
        assertThat(BudgetHealth.of(1.01)).isEqualTo(BudgetHealth.OVER);
    }
}
