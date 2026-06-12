package com.simpleerp.finance;

import com.simpleerp.finance.dto.VendorRequest;
import com.simpleerp.finance.dto.VendorResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoints for vendors (AP parties). */
@RestController
@RequestMapping("/api/v1/finance/vendors")
public class VendorController {

    private final VendorService service;

    public VendorController(VendorService service) {
        this.service = service;
    }

    /** Lists all vendors. */
    @GetMapping
    public List<VendorResponse> list() {
        return service.list().stream().map(VendorResponse::from).toList();
    }

    /** Creates a vendor and returns 201 with its location. */
    @PostMapping
    public ResponseEntity<VendorResponse> create(@Valid @RequestBody VendorRequest request) {
        Vendor vendor = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/finance/vendors/" + vendor.getId()))
                .body(VendorResponse.from(vendor));
    }
}
