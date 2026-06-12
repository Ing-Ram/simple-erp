package com.simpleerp.finance;

import java.math.BigDecimal;

/** Payments-applied total for one document (invoice or bill), used to derive outstanding balance. */
public record PaidTotal(Long documentId, BigDecimal paid) {
}
