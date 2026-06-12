package com.simpleerp.finance;

import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for vendors. */
public interface VendorRepository extends JpaRepository<Vendor, Long> {
}
