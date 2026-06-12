package com.simpleerp.finance;

import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for customers. */
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
