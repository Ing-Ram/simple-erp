package com.simpleerp.sales.dto;

import com.simpleerp.sales.SalesOrder;
import com.simpleerp.sales.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Sales order representation returned to clients; names are resolved by the service. */
public record SalesOrderResponse(
        Long id,
        Long customerId,
        String customerName,
        Long ownerEmployeeId,
        String ownerName,
        OrderStatus status,
        LocalDate orderDate,
        Long opportunityId,
        Long invoiceId,
        BigDecimal total,
        String currency,
        List<LineResponse> lines) {

    /** One order line as returned to clients. */
    public record LineResponse(Long id, String description, BigDecimal quantity, BigDecimal unitPrice) {
    }

    /** Maps an entity to its response shape, given the cross-module names the service resolved. */
    public static SalesOrderResponse from(SalesOrder o, String customerName, String ownerName) {
        List<LineResponse> lines = o.getLines().stream()
                .map(l -> new LineResponse(l.getId(), l.getDescription(), l.getQuantity(),
                        l.getUnitPrice().getAmount()))
                .toList();
        return new SalesOrderResponse(
                o.getId(),
                o.getCustomerId(),
                customerName,
                o.getOwnerEmployeeId(),
                ownerName,
                o.getStatus(),
                o.getOrderDate(),
                o.getOpportunityId(),
                o.getInvoiceId(),
                o.total().getAmount(),
                o.total().getCurrency(),
                lines);
    }
}
