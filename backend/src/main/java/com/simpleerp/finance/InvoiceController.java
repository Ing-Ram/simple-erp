package com.simpleerp.finance;

import com.simpleerp.finance.dto.InvoiceRequest;
import com.simpleerp.finance.dto.InvoiceResponse;
import com.simpleerp.finance.dto.PaymentRequest;
import com.simpleerp.finance.dto.PaymentResponse;
import com.simpleerp.shared.PagedResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoints for AR invoices; a thin layer that delegates all logic to the service. */
@RestController
@RequestMapping("/api/v1/finance/invoices")
public class InvoiceController {

    private final InvoiceService service;

    public InvoiceController(InvoiceService service) {
        this.service = service;
    }

    /** Creates a draft invoice and returns 201 with its location. */
    @PostMapping
    public ResponseEntity<InvoiceResponse> create(@Valid @RequestBody InvoiceRequest request) {
        InvoiceResponse invoice = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/finance/invoices/" + invoice.id()))
                .body(invoice);
    }

    /** Returns a page of invoices, most recently issued first. */
    @GetMapping
    public PagedResponse<InvoiceResponse> list(
            @PageableDefault(size = 20, sort = "issueDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return PagedResponse.from(service.list(pageable));
    }

    /** Returns one invoice. */
    @GetMapping("/{id}")
    public InvoiceResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    /** Transitions a draft invoice to SENT. */
    @PostMapping("/{id}/send")
    public InvoiceResponse send(@PathVariable Long id) {
        return service.send(id);
    }

    /** Voids an invoice that has no payments applied. */
    @PostMapping("/{id}/void")
    public InvoiceResponse voidInvoice(@PathVariable Long id) {
        return service.voidInvoice(id);
    }

    /** Records a payment against the invoice. */
    @PostMapping("/{id}/payments")
    public InvoiceResponse recordPayment(@PathVariable Long id, @Valid @RequestBody PaymentRequest request) {
        return service.recordPayment(id, request);
    }

    /** Payment history for the invoice, most recent first. */
    @GetMapping("/{id}/payments")
    public List<PaymentResponse> payments(@PathVariable Long id) {
        return service.payments(id);
    }

    /** AR aging buckets as of today. */
    @GetMapping("/aging")
    public List<AgingBucket> aging() {
        return service.arAging();
    }
}
