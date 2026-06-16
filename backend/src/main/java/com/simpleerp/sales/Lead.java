package com.simpleerp.sales;

import com.simpleerp.shared.AuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * A potential customer at the top of the funnel. Qualifying a lead creates a Customer (via Finance)
 * and an Opportunity, recording their ids here.
 */
@Entity
@Table(name = "leads")
public class Lead extends AuditableEntity {

    private String name;
    private String company;
    private String email;

    @Enumerated(EnumType.STRING)
    private LeadSource source;

    @Enumerated(EnumType.STRING)
    private LeadStatus status = LeadStatus.NEW;

    /** Set when the lead is qualified — the customer it became, owned by Finance. */
    private Long customerId;

    /** Set when the lead is qualified — the opportunity it spawned. */
    private Long opportunityId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LeadSource getSource() {
        return source;
    }

    public void setSource(LeadSource source) {
        this.source = source;
    }

    public LeadStatus getStatus() {
        return status;
    }

    public void setStatus(LeadStatus status) {
        this.status = status;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getOpportunityId() {
        return opportunityId;
    }

    public void setOpportunityId(Long opportunityId) {
        this.opportunityId = opportunityId;
    }
}
