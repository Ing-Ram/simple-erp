package com.simpleerp.sales;

import com.simpleerp.sales.dto.LoseRequest;
import com.simpleerp.sales.dto.OpportunityRequest;
import com.simpleerp.sales.dto.OpportunityResponse;
import com.simpleerp.sales.dto.StageChangeRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoints for opportunities: create, advance, win, lose, reopen. */
@RestController
@RequestMapping("/api/v1/sales/opportunities")
public class OpportunityController {

    private final OpportunityService service;

    public OpportunityController(OpportunityService service) {
        this.service = service;
    }

    /** Lists all opportunities. */
    @GetMapping
    public List<OpportunityResponse> list() {
        return service.list();
    }

    /** Returns one opportunity. */
    @GetMapping("/{id}")
    public OpportunityResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    /** Creates an opportunity and returns 201 with its location. */
    @PostMapping
    public ResponseEntity<OpportunityResponse> create(@Valid @RequestBody OpportunityRequest request) {
        OpportunityResponse opportunity = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/sales/opportunities/" + opportunity.id()))
                .body(opportunity);
    }

    /** Advances the opportunity to a later open stage. */
    @PostMapping("/{id}/advance")
    public OpportunityResponse advance(@PathVariable Long id, @Valid @RequestBody StageChangeRequest request) {
        return service.advance(id, request.stage());
    }

    /** Wins the opportunity, creating its sales order in the same transaction. */
    @PostMapping("/{id}/win")
    public OpportunityResponse win(@PathVariable Long id) {
        return service.win(id);
    }

    /** Marks the opportunity lost; a reason is required. */
    @PostMapping("/{id}/lose")
    public OpportunityResponse lose(@PathVariable Long id, @Valid @RequestBody LoseRequest request) {
        return service.lose(id, request.lostReason());
    }

    /** Reopens a lost opportunity to its previous stage. */
    @PostMapping("/{id}/reopen")
    public OpportunityResponse reopen(@PathVariable Long id) {
        return service.reopen(id);
    }
}
