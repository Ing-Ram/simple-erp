package com.simpleerp.hr;

import com.simpleerp.hr.dto.LeaveDecisionRequest;
import com.simpleerp.hr.dto.LeaveRequestRequest;
import com.simpleerp.hr.dto.LeaveRequestResponse;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoints for the leave workflow: submit, approve, reject, cancel. */
@RestController
@RequestMapping("/api/v1/hr/leave-requests")
public class LeaveRequestController {

    private final LeaveRequestService service;

    public LeaveRequestController(LeaveRequestService service) {
        this.service = service;
    }

    /** Submits a leave request and returns 201 with its location. */
    @PostMapping
    public ResponseEntity<LeaveRequestResponse> submit(@Valid @RequestBody LeaveRequestRequest request) {
        LeaveRequestResponse leave = service.submit(request);
        return ResponseEntity
                .created(URI.create("/api/v1/hr/leave-requests/" + leave.id()))
                .body(leave);
    }

    /** Approves a pending request. An optional body names the reviewer. */
    @PostMapping("/{id}/approve")
    public LeaveRequestResponse approve(@PathVariable Long id,
                                        @RequestBody(required = false) LeaveDecisionRequest decision) {
        return service.approve(id, decision == null ? null : decision.reviewer());
    }

    /** Rejects a pending request. An optional body names the reviewer. */
    @PostMapping("/{id}/reject")
    public LeaveRequestResponse reject(@PathVariable Long id,
                                       @RequestBody(required = false) LeaveDecisionRequest decision) {
        return service.reject(id, decision == null ? null : decision.reviewer());
    }

    /** Cancels approved leave that has not yet started. */
    @PostMapping("/{id}/cancel")
    public LeaveRequestResponse cancel(@PathVariable Long id) {
        return service.cancel(id);
    }
}
