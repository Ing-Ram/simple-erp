package com.simpleerp.sales;

import com.simpleerp.finance.CustomerService;
import com.simpleerp.hr.EmployeeService;
import com.simpleerp.sales.dto.OpportunityRequest;
import com.simpleerp.sales.dto.OpportunityResponse;
import com.simpleerp.shared.InvalidStateException;
import com.simpleerp.shared.Money;
import com.simpleerp.shared.NotFoundException;
import com.simpleerp.shared.ValidationException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Opportunity lifecycle: create, advance through open stages, win (which raises the order in the
 * same transaction), lose (with a required reason), and reopen. Customer and owner are validated
 * and named through Finance's CustomerService and HR's EmployeeService — never their repositories.
 */
@Service
@Transactional
public class OpportunityService {

    private final OpportunityRepository opportunities;
    private final SalesOrderService salesOrders;
    private final CustomerService customers;
    private final EmployeeService employees;

    public OpportunityService(OpportunityRepository opportunities, SalesOrderService salesOrders,
                              CustomerService customers, EmployeeService employees) {
        this.opportunities = opportunities;
        this.salesOrders = salesOrders;
        this.customers = customers;
        this.employees = employees;
    }

    /** Creates a PROSPECTING opportunity for an existing customer. */
    public OpportunityResponse create(OpportunityRequest request) {
        Opportunity opportunity = open(
                request.customerId(), request.ownerEmployeeId(),
                new Money(request.expectedValue(), request.currency()),
                request.probability(), request.expectedCloseDate(), OpportunityStage.PROSPECTING);
        return toResponse(opportunity);
    }

    /**
     * Opens an opportunity at the given stage after validating the customer and owner exist.
     * Shared by {@link #create} and lead qualification; returns the saved entity.
     */
    Opportunity open(Long customerId, Long ownerEmployeeId, Money expectedValue, int probability,
                     LocalDate expectedCloseDate, OpportunityStage stage) {
        customers.get(customerId);   // throws 404 if the customer does not exist
        employees.get(ownerEmployeeId);
        Opportunity opportunity = new Opportunity();
        opportunity.setCustomerId(customerId);
        opportunity.setOwnerEmployeeId(ownerEmployeeId);
        opportunity.setExpectedValue(expectedValue);
        opportunity.setProbability(probability);
        opportunity.setExpectedCloseDate(expectedCloseDate);
        opportunity.setStage(stage);
        return opportunities.save(opportunity);
    }

    /** Advances an open opportunity to a later open stage; stages move only forward. */
    public OpportunityResponse advance(Long id, OpportunityStage target) {
        Opportunity opportunity = load(id);
        if (!opportunity.getStage().isOpen()) {
            throw new InvalidStateException("Opportunity " + id + " is closed (" + opportunity.getStage() + ")");
        }
        if (!target.isOpen()) {
            throw new ValidationException("Use win or lose to close an opportunity");
        }
        if (target.ordinal() <= opportunity.getStage().ordinal()) {
            throw new InvalidStateException("Stages move only forward");
        }
        opportunity.setStage(target);
        return toResponse(opportunity);
    }

    /** Wins an opportunity, creating its sales order in the same transaction. Wins exactly once. */
    public OpportunityResponse win(Long id) {
        Opportunity opportunity = load(id);
        switch (opportunity.getStage()) {
            case PROSPECTING, QUALIFIED, PROPOSAL, NEGOTIATION -> {
                SalesOrder order = salesOrders.createFromWonOpportunity(opportunity);
                opportunity.setSalesOrderId(order.getId());
                opportunity.setStage(OpportunityStage.WON);
                opportunity.setClosedDate(LocalDate.now());
            }
            case WON, LOST ->
                    throw new InvalidStateException("Opportunity " + id + " is already " + opportunity.getStage());
        }
        return toResponse(opportunity);
    }

    /** Marks an open opportunity lost; a reason is required and the prior stage is remembered. */
    public OpportunityResponse lose(Long id, String lostReason) {
        if (lostReason == null || lostReason.isBlank()) {
            throw new ValidationException("A reason is required to mark an opportunity lost");
        }
        Opportunity opportunity = load(id);
        switch (opportunity.getStage()) {
            case PROSPECTING, QUALIFIED, PROPOSAL, NEGOTIATION -> {
                opportunity.setPreviousStage(opportunity.getStage());
                opportunity.setStage(OpportunityStage.LOST);
                opportunity.setLostReason(lostReason);
                opportunity.setClosedDate(LocalDate.now());
            }
            case WON, LOST ->
                    throw new InvalidStateException("Opportunity " + id + " is already " + opportunity.getStage());
        }
        return toResponse(opportunity);
    }

    /** Reopens a lost opportunity back to the stage it held before. */
    public OpportunityResponse reopen(Long id) {
        Opportunity opportunity = load(id);
        switch (opportunity.getStage()) {
            case LOST -> {
                OpportunityStage prior = opportunity.getPreviousStage();
                opportunity.setStage(prior == null ? OpportunityStage.PROSPECTING : prior);
                opportunity.setPreviousStage(null);
                opportunity.setLostReason(null);
                opportunity.setClosedDate(null);
            }
            case PROSPECTING, QUALIFIED, PROPOSAL, NEGOTIATION, WON ->
                    throw new InvalidStateException("Only a lost opportunity can be reopened");
        }
        return toResponse(opportunity);
    }

    /** All opportunities. */
    @Transactional(readOnly = true)
    public List<OpportunityResponse> list() {
        return opportunities.findAll().stream().map(this::toResponse).toList();
    }

    /** Loads one opportunity as a response, or throws 404. */
    @Transactional(readOnly = true)
    public OpportunityResponse get(Long id) {
        return toResponse(load(id));
    }

    /**
     * Closed deals (won and lost), most recently closed first, optionally for a single salesperson.
     * Each row carries full detail — customer, owner, value, close date, and the reason if lost.
     */
    @Transactional(readOnly = true)
    public List<OpportunityResponse> closedDeals(Long ownerEmployeeId) {
        List<OpportunityStage> closed = List.of(OpportunityStage.WON, OpportunityStage.LOST);
        List<Opportunity> deals = ownerEmployeeId == null
                ? opportunities.findByStageInOrderByClosedDateDesc(closed)
                : opportunities.findByStageInAndOwnerEmployeeIdOrderByClosedDateDesc(closed, ownerEmployeeId);
        return deals.stream().map(this::toResponse).toList();
    }

    private Opportunity load(Long id) {
        return opportunities.findById(id).orElseThrow(() -> new NotFoundException("Opportunity", id));
    }

    /** Resolves the cross-module names and maps to the response. */
    private OpportunityResponse toResponse(Opportunity o) {
        String customerName = customers.get(o.getCustomerId()).getName();
        String ownerName = employees.get(o.getOwnerEmployeeId()).name();
        return OpportunityResponse.from(o, customerName, ownerName);
    }
}
