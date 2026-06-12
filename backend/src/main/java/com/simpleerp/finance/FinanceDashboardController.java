package com.simpleerp.finance;

import com.simpleerp.finance.dto.FinanceDashboardResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only Finance dashboard endpoint: the whole summary in one call. */
@RestController
@RequestMapping("/api/v1/finance/dashboard")
public class FinanceDashboardController {

    private final FinanceDashboardService service;

    public FinanceDashboardController(FinanceDashboardService service) {
        this.service = service;
    }

    /** Returns the Finance dashboard summary as of today. */
    @GetMapping
    public FinanceDashboardResponse dashboard() {
        return service.summary();
    }
}
