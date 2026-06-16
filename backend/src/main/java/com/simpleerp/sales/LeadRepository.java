package com.simpleerp.sales;

import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for leads. */
public interface LeadRepository extends JpaRepository<Lead, Long> {
}
