package com.simpleerp.sales.dto;

import com.simpleerp.sales.Lead;
import com.simpleerp.sales.LeadSource;
import com.simpleerp.sales.LeadStatus;

/** Lead representation returned to clients. */
public record LeadResponse(
        Long id,
        String name,
        String company,
        String email,
        LeadSource source,
        LeadStatus status,
        Long customerId,
        Long opportunityId) {

    /** Maps an entity to its response shape; the single home for this mapping. */
    public static LeadResponse from(Lead l) {
        return new LeadResponse(
                l.getId(), l.getName(), l.getCompany(), l.getEmail(),
                l.getSource(), l.getStatus(), l.getCustomerId(), l.getOpportunityId());
    }
}
