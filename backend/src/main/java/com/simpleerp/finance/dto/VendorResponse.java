package com.simpleerp.finance.dto;

import com.simpleerp.finance.Vendor;

/** Vendor representation returned to clients. */
public record VendorResponse(Long id, String name, String email, int paymentTermsDays, boolean active) {

    /** Maps an entity to its response shape; the single home for this mapping. */
    public static VendorResponse from(Vendor v) {
        return new VendorResponse(v.getId(), v.getName(), v.getEmail(), v.getPaymentTermsDays(), v.isActive());
    }
}
