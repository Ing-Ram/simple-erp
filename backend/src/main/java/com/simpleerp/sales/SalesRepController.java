package com.simpleerp.sales;

import com.simpleerp.sales.dto.RepPerformanceResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Read-only per-salesperson deal performance. */
@RestController
@RequestMapping("/api/v1/sales/reps")
public class SalesRepController {

    private final SalesRepService service;

    public SalesRepController(SalesRepService service) {
        this.service = service;
    }

    /**
     * Each salesperson's won/lost counts, won and average value, win rate, and open pipeline.
     * {@code period} scopes the won/lost figures: {@code all} (default), {@code quarter}
     * (current calendar quarter), or {@code last90} (trailing 90 days).
     */
    @GetMapping
    public List<RepPerformanceResponse> performance(
            @RequestParam(defaultValue = "all") String period) {
        return service.performance(sinceFor(period));
    }

    /** Resolves a period code to the date won/lost deals are counted from (null = all time). */
    private LocalDate sinceFor(String period) {
        LocalDate today = LocalDate.now();
        return switch (period) {
            case "quarter" -> LocalDate.of(today.getYear(), ((today.getMonthValue() - 1) / 3) * 3 + 1, 1);
            case "last90" -> today.minusDays(90);
            default -> null;
        };
    }
}
