package com.simpleerp.sales;

import com.simpleerp.sales.dto.LeadRequest;
import com.simpleerp.sales.dto.LeadResponse;
import com.simpleerp.sales.dto.QualifyLeadRequest;
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

/** REST endpoints for leads and their qualification. */
@RestController
@RequestMapping("/api/v1/sales/leads")
public class LeadController {

    private final LeadService service;

    public LeadController(LeadService service) {
        this.service = service;
    }

    /** Lists all leads. */
    @GetMapping
    public List<LeadResponse> list() {
        return service.list();
    }

    /** Captures a lead and returns 201 with its location. */
    @PostMapping
    public ResponseEntity<LeadResponse> create(@Valid @RequestBody LeadRequest request) {
        LeadResponse lead = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/sales/leads/" + lead.id())).body(lead);
    }

    /** Marks a new lead as contacted. */
    @PostMapping("/{id}/contact")
    public LeadResponse contact(@PathVariable Long id) {
        return service.markContacted(id);
    }

    /** Qualifies a lead into a customer and an opportunity. */
    @PostMapping("/{id}/qualify")
    public LeadResponse qualify(@PathVariable Long id, @Valid @RequestBody QualifyLeadRequest request) {
        return service.qualify(id, request);
    }

    /** Disqualifies a lead. */
    @PostMapping("/{id}/disqualify")
    public LeadResponse disqualify(@PathVariable Long id) {
        return service.disqualify(id);
    }
}
