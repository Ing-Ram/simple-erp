package com.simpleerp.sales;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for sales orders. */
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    /** Orders in a given status — e.g. FULFILLED orders awaiting invoicing for needs-attention. */
    List<SalesOrder> findByStatus(OrderStatus status);
}
