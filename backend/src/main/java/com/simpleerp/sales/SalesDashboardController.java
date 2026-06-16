package com.simpleerp.sales;

import com.simpleerp.sales.dto.SalesDashboardResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only Sales dashboard endpoint: the whole summary in one call. */
@RestController
@RequestMapping("/api/v1/sales/dashboard")
public class SalesDashboardController {

    private final SalesDashboardService service;

    public SalesDashboardController(SalesDashboardService service) {
        this.service = service;
    }

    /** Returns the Sales dashboard summary as of today. */
    @GetMapping
    public SalesDashboardResponse dashboard() {
        return service.summary();
    }
}
