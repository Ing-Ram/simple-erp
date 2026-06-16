package com.simpleerp.sales;

import com.simpleerp.sales.dto.SalesOrderResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoints for sales orders: fulfil, invoice (hands off to Finance), cancel. */
@RestController
@RequestMapping("/api/v1/sales/orders")
public class SalesOrderController {

    private final SalesOrderService service;

    public SalesOrderController(SalesOrderService service) {
        this.service = service;
    }

    /** Lists all orders. */
    @GetMapping
    public List<SalesOrderResponse> list() {
        return service.list();
    }

    /** Returns one order. */
    @GetMapping("/{id}")
    public SalesOrderResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    /** Marks an open order fulfilled. */
    @PostMapping("/{id}/fulfill")
    public SalesOrderResponse fulfill(@PathVariable Long id) {
        return service.fulfill(id);
    }

    /** Invoices a fulfilled order, creating the AR invoice in Finance. */
    @PostMapping("/{id}/invoice")
    public SalesOrderResponse invoice(@PathVariable Long id) {
        return service.invoice(id);
    }

    /** Cancels an order that has not yet been fulfilled. */
    @PostMapping("/{id}/cancel")
    public SalesOrderResponse cancel(@PathVariable Long id) {
        return service.cancel(id);
    }
}
