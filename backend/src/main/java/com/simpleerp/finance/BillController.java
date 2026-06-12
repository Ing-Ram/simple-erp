package com.simpleerp.finance;

import com.simpleerp.finance.dto.BillPaymentRequest;
import com.simpleerp.finance.dto.BillPaymentResponse;
import com.simpleerp.finance.dto.BillRequest;
import com.simpleerp.finance.dto.BillResponse;
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

/** REST endpoints for AP bills; a thin layer that delegates all logic to the service. */
@RestController
@RequestMapping("/api/v1/finance/bills")
public class BillController {

    private final BillService service;

    public BillController(BillService service) {
        this.service = service;
    }

    /** Creates a draft bill and returns 201 with its location. */
    @PostMapping
    public ResponseEntity<BillResponse> create(@Valid @RequestBody BillRequest request) {
        BillResponse bill = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/finance/bills/" + bill.id()))
                .body(bill);
    }

    /** Returns a page of bills, most recently issued first. */
    @GetMapping
    public PagedResponse<BillResponse> list(
            @PageableDefault(size = 20, sort = "issueDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return PagedResponse.from(service.list(pageable));
    }

    /** Returns one bill. */
    @GetMapping("/{id}")
    public BillResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    /** Transitions a draft bill to SENT. */
    @PostMapping("/{id}/send")
    public BillResponse send(@PathVariable Long id) {
        return service.send(id);
    }

    /** Voids a bill that has no payments applied. */
    @PostMapping("/{id}/void")
    public BillResponse voidBill(@PathVariable Long id) {
        return service.voidBill(id);
    }

    /** Records a payment against the bill. */
    @PostMapping("/{id}/payments")
    public BillResponse recordPayment(@PathVariable Long id, @Valid @RequestBody BillPaymentRequest request) {
        return service.recordPayment(id, request);
    }

    /** Payment history for the bill, most recent first. */
    @GetMapping("/{id}/payments")
    public List<BillPaymentResponse> payments(@PathVariable Long id) {
        return service.payments(id);
    }

    /** AP aging buckets as of today. */
    @GetMapping("/aging")
    public List<AgingBucket> aging() {
        return service.apAging();
    }
}
