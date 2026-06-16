package com.simpleerp.sales;

import java.math.BigDecimal;
import java.time.LocalDate;

/** A won opportunity's close date and value, used to build trailing monthly-won revenue. */
public record WonAmount(LocalDate closedDate, BigDecimal amount) {
}
