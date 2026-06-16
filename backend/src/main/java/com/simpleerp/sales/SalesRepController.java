package com.simpleerp.sales;

import com.simpleerp.sales.dto.RepPerformanceResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only per-salesperson deal performance. */
@RestController
@RequestMapping("/api/v1/sales/reps")
public class SalesRepController {

    private final SalesRepService service;

    public SalesRepController(SalesRepService service) {
        this.service = service;
    }

    /** Each salesperson's won/lost counts, won and average value, win rate, and open pipeline. */
    @GetMapping
    public List<RepPerformanceResponse> performance() {
        return service.performance();
    }
}
