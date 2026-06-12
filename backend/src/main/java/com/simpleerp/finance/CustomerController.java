package com.simpleerp.finance;

import com.simpleerp.finance.dto.CustomerRequest;
import com.simpleerp.finance.dto.CustomerResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoints for customers (AR parties). */
@RestController
@RequestMapping("/api/v1/finance/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    /** Lists all customers. */
    @GetMapping
    public List<CustomerResponse> list() {
        return service.list().stream().map(CustomerResponse::from).toList();
    }

    /** Creates a customer and returns 201 with its location. */
    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        Customer customer = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/finance/customers/" + customer.getId()))
                .body(CustomerResponse.from(customer));
    }
}
