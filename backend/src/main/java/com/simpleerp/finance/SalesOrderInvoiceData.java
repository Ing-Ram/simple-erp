package com.simpleerp.finance;

import java.math.BigDecimal;
import java.util.List;

/**
 * The data Finance needs to raise an AR invoice from a fulfilled sales order. Defined here, in
 * Finance, so Sales depends on Finance's published API rather than the reverse — Finance never sees
 * a Sales entity. This is the second factory the AP/AR design reserved room for.
 */
public record SalesOrderInvoiceData(Long salesOrderId, Long customerId, List<Line> lines) {

    /** One order line to bill. */
    public record Line(String description, BigDecimal quantity, BigDecimal unitPrice, String currency) {
    }
}
