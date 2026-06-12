package com.simpleerp.finance;

import java.math.BigDecimal;

/**
 * One aging-bucket aggregate row produced by a repository query.
 *
 * <p>Bucket codes are {@code CURRENT}, {@code D1_30}, {@code D31_60}, {@code D61_90}, and
 * {@code D90_PLUS}. The same shape is used for both AR (invoices) and AP (bills).
 */
public record AgingBucket(String bucket, long documentCount, BigDecimal outstanding) {
}
