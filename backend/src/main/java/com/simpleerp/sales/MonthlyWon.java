package com.simpleerp.sales;

import java.math.BigDecimal;

/** Won revenue for one calendar month, e.g. {@code month = "2026-06"}. */
public record MonthlyWon(String month, BigDecimal amount) {
}
