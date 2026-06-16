package com.simpleerp.sales;

import java.math.BigDecimal;

/** Count and total expected value of open opportunities in one pipeline stage. */
public record StageFunnel(OpportunityStage stage, long count, BigDecimal value) {
}
