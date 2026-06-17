package com.simpleerp.projects;

/**
 * Derived budget health of a project — never stored. Thresholds on percent consumed (spent ÷ budget):
 * ON_TRACK (&lt; 80%), AT_RISK (80–100%), OVER (&gt; 100%).
 */
public enum BudgetHealth {
    ON_TRACK, AT_RISK, OVER;

    /** Classifies a percent-consumed ratio (e.g. 0.85 = 85%) into a health band. */
    public static BudgetHealth of(double percentConsumed) {
        if (percentConsumed > 1.0) {
            return OVER;
        }
        return percentConsumed >= 0.8 ? AT_RISK : ON_TRACK;
    }
}
