package com.simpleerp.finance;

import com.simpleerp.finance.dto.VendorRequest;
import com.simpleerp.shared.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Vendor management for the AP side. */
@Service
@Transactional
public class VendorService {

    private final VendorRepository vendors;

    public VendorService(VendorRepository vendors) {
        this.vendors = vendors;
    }

    /** Creates an active vendor from the request payload. */
    public Vendor create(VendorRequest request) {
        Vendor vendor = new Vendor();
        vendor.setName(request.name());
        vendor.setEmail(request.email());
        vendor.setPaymentTermsDays(request.paymentTermsDays());
        vendor.setActive(true);
        return vendors.save(vendor);
    }

    /** All vendors, for selectors and lists. */
    @Transactional(readOnly = true)
    public List<Vendor> list() {
        return vendors.findAll();
    }

    /** Loads a vendor or throws 404. */
    @Transactional(readOnly = true)
    public Vendor get(Long id) {
        return vendors.findById(id).orElseThrow(() -> new NotFoundException("Vendor", id));
    }
}
