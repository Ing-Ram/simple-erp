package com.simpleerp.sales.dto;

import com.simpleerp.sales.Opportunity;
import com.simpleerp.sales.OpportunityStage;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Opportunity representation returned to clients; names are resolved by the service. */
public record OpportunityResponse(
        Long id,
        Long customerId,
        String customerName,
        Long ownerEmployeeId,
        String ownerName,
        BigDecimal expectedValue,
        BigDecimal weightedValue,
        String currency,
        int probability,
        LocalDate expectedCloseDate,
        OpportunityStage stage,
        String lostReason,
        LocalDate closedDate,
        Long salesOrderId) {

    /** Maps an entity to its response shape, given the cross-module names the service resolved. */
    public static OpportunityResponse from(Opportunity o, String customerName, String ownerName) {
        return new OpportunityResponse(
                o.getId(),
                o.getCustomerId(),
                customerName,
                o.getOwnerEmployeeId(),
                ownerName,
                o.getExpectedValue().getAmount(),
                o.weightedValue().getAmount(),
                o.getExpectedValue().getCurrency(),
                o.getProbability(),
                o.getExpectedCloseDate(),
                o.getStage(),
                o.getLostReason(),
                o.getClosedDate(),
                o.getSalesOrderId());
    }
}
