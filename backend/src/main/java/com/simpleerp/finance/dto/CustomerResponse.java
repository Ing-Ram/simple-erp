package com.simpleerp.finance.dto;

import com.simpleerp.finance.Customer;

/** Customer representation returned to clients. */
public record CustomerResponse(Long id, String name, String email, int paymentTermsDays, boolean active) {

    /** Maps an entity to its response shape; the single home for this mapping. */
    public static CustomerResponse from(Customer c) {
        return new CustomerResponse(c.getId(), c.getName(), c.getEmail(), c.getPaymentTermsDays(), c.isActive());
    }
}
