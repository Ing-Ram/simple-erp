package com.simpleerp.sales;

import com.simpleerp.finance.CustomerService;
import com.simpleerp.finance.dto.CustomerRequest;
import com.simpleerp.sales.dto.LeadRequest;
import com.simpleerp.sales.dto.LeadResponse;
import com.simpleerp.sales.dto.QualifyLeadRequest;
import com.simpleerp.shared.InvalidStateException;
import com.simpleerp.shared.Money;
import com.simpleerp.shared.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lead capture and qualification. Qualifying converts a lead into a customer and an opportunity. */
@Service
@Transactional
public class LeadService {

    private final LeadRepository leads;
    private final CustomerService customers;
    private final OpportunityService opportunities;

    public LeadService(LeadRepository leads, CustomerService customers, OpportunityService opportunities) {
        this.leads = leads;
        this.customers = customers;
        this.opportunities = opportunities;
    }

    /** Captures a new lead. */
    public LeadResponse create(LeadRequest request) {
        Lead lead = new Lead();
        lead.setName(request.name());
        lead.setCompany(request.company());
        lead.setEmail(request.email());
        lead.setSource(request.source());
        lead.setStatus(LeadStatus.NEW);
        return LeadResponse.from(leads.save(lead));
    }

    /** Marks a new lead as contacted. */
    public LeadResponse markContacted(Long id) {
        Lead lead = load(id);
        switch (lead.getStatus()) {
            case NEW -> lead.setStatus(LeadStatus.CONTACTED);
            case CONTACTED, QUALIFIED, DISQUALIFIED ->
                    throw new InvalidStateException("Cannot contact a lead in status " + lead.getStatus());
        }
        return LeadResponse.from(lead);
    }

    /**
     * Qualifies a lead: creates a Customer (via Finance) and a QUALIFIED opportunity owned by the
     * given employee, then records both ids on the lead.
     */
    public LeadResponse qualify(Long id, QualifyLeadRequest request) {
        Lead lead = load(id);
        switch (lead.getStatus()) {
            case NEW, CONTACTED -> {
                String customerName = lead.getCompany() == null || lead.getCompany().isBlank()
                        ? lead.getName() : lead.getCompany();
                var customer = customers.create(
                        new CustomerRequest(customerName, lead.getEmail(), request.paymentTermsDays()));
                Opportunity opportunity = opportunities.open(
                        customer.getId(), request.ownerEmployeeId(),
                        new Money(request.expectedValue(), request.currency()),
                        request.probability(), request.expectedCloseDate(), OpportunityStage.QUALIFIED);
                lead.setCustomerId(customer.getId());
                lead.setOpportunityId(opportunity.getId());
                lead.setStatus(LeadStatus.QUALIFIED);
            }
            case QUALIFIED, DISQUALIFIED ->
                    throw new InvalidStateException("Cannot qualify a lead in status " + lead.getStatus());
        }
        return LeadResponse.from(lead);
    }

    /** Disqualifies a lead that will not convert. */
    public LeadResponse disqualify(Long id) {
        Lead lead = load(id);
        switch (lead.getStatus()) {
            case NEW, CONTACTED -> lead.setStatus(LeadStatus.DISQUALIFIED);
            case QUALIFIED, DISQUALIFIED ->
                    throw new InvalidStateException("Cannot disqualify a lead in status " + lead.getStatus());
        }
        return LeadResponse.from(lead);
    }

    /** All leads. */
    @Transactional(readOnly = true)
    public List<LeadResponse> list() {
        return leads.findAll().stream().map(LeadResponse::from).toList();
    }

    private Lead load(Long id) {
        return leads.findById(id).orElseThrow(() -> new NotFoundException("Lead", id));
    }
}
