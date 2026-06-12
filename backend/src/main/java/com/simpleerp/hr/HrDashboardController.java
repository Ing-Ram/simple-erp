package com.simpleerp.hr;

import com.simpleerp.hr.dto.HrDashboardResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only HR dashboard endpoint: the whole summary in one call. */
@RestController
@RequestMapping("/api/v1/hr/dashboard")
public class HrDashboardController {

    private final HrDashboardService service;

    public HrDashboardController(HrDashboardService service) {
        this.service = service;
    }

    /** Returns the HR dashboard summary as of today. */
    @GetMapping
    public HrDashboardResponse dashboard() {
        return service.summary();
    }
}
