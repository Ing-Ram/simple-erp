package com.simpleerp.hr;

import com.simpleerp.hr.dto.CheckInRequest;
import com.simpleerp.hr.dto.PresenceResponse;
import com.simpleerp.hr.dto.RollCallResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Building presence: check-in / check-out and the emergency roll-call. */
@RestController
@RequestMapping("/api/v1/hr/presence")
public class PresenceController {

    private final PresenceService service;

    public PresenceController(PresenceService service) {
        this.service = service;
    }

    /** Checks an employee in for the day (on site or remote). */
    @PostMapping("/check-in")
    public PresenceResponse checkIn(@Valid @RequestBody CheckInRequest request) {
        return service.checkIn(request);
    }

    /** Checks an employee out of the building. */
    @PostMapping("/{employeeId}/check-out")
    public PresenceResponse checkOut(@PathVariable Long employeeId) {
        return service.checkOut(employeeId);
    }

    /** The emergency roll-call: every active employee reconciled into an accountability status. */
    @GetMapping("/roll-call")
    public RollCallResponse rollCall() {
        return service.rollCall();
    }
}
