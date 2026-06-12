package com.simpleerp.finance;

import com.simpleerp.finance.dto.CustomerRequest;
import com.simpleerp.shared.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Customer management and the cross-module lookup seam.
 *
 * <p>Other modules (e.g. Sales qualifying a lead) reach customers through this service, never
 * through {@link CustomerRepository} directly, so the monolith stays splittable.
 */
@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customers;

    public CustomerService(CustomerRepository customers) {
        this.customers = customers;
    }

    /** Creates an active customer from the request payload. */
    public Customer create(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setName(request.name());
        customer.setEmail(request.email());
        customer.setPaymentTermsDays(request.paymentTermsDays());
        customer.setActive(true);
        return customers.save(customer);
    }

    /** All customers, for selectors and lists. */
    @Transactional(readOnly = true)
    public List<Customer> list() {
        return customers.findAll();
    }

    /** Loads a customer or throws 404. */
    @Transactional(readOnly = true)
    public Customer get(Long id) {
        return customers.findById(id).orElseThrow(() -> new NotFoundException("Customer", id));
    }
}
