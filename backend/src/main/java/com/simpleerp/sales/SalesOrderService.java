package com.simpleerp.sales;

import com.simpleerp.finance.CustomerService;
import com.simpleerp.finance.InvoiceService;
import com.simpleerp.finance.SalesOrderInvoiceData;
import com.simpleerp.hr.EmployeeService;
import com.simpleerp.sales.dto.SalesOrderResponse;
import com.simpleerp.shared.InvalidStateException;
import com.simpleerp.shared.NotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sales order lifecycle: created by winning an opportunity, fulfilled, then invoiced — the last step
 * handing off to Finance's {@link InvoiceService#createFromOrder}. Cross-module access is only ever
 * through service interfaces.
 */
@Service
@Transactional
public class SalesOrderService {

    private final SalesOrderRepository orders;
    private final InvoiceService invoices;
    private final CustomerService customers;
    private final EmployeeService employees;

    public SalesOrderService(SalesOrderRepository orders, InvoiceService invoices,
                             CustomerService customers, EmployeeService employees) {
        this.orders = orders;
        this.invoices = invoices;
        this.customers = customers;
        this.employees = employees;
    }

    /**
     * Builds an OPEN order from a won opportunity. v1 carries a single line for the opportunity's
     * expected value; richer line items can be added before fulfilment.
     */
    SalesOrder createFromWonOpportunity(Opportunity opportunity) {
        SalesOrder order = new SalesOrder();
        order.setCustomerId(opportunity.getCustomerId());
        order.setOwnerEmployeeId(opportunity.getOwnerEmployeeId());
        order.setOrderDate(LocalDate.now());
        order.setOpportunityId(opportunity.getId());
        SalesOrderLine line = new SalesOrderLine();
        line.setOrder(order);
        line.setDescription("Won opportunity #" + opportunity.getId());
        line.setQuantity(BigDecimal.ONE);
        line.setUnitPrice(opportunity.getExpectedValue());
        order.getLines().add(line);
        return orders.save(order);
    }

    /** Marks an open order fulfilled. */
    public SalesOrderResponse fulfill(Long id) {
        SalesOrder order = load(id);
        switch (order.getStatus()) {
            case OPEN -> order.setStatus(OrderStatus.FULFILLED);
            case FULFILLED, INVOICED, CANCELLED ->
                    throw new InvalidStateException("Cannot fulfill an order in status " + order.getStatus());
        }
        return toResponse(order);
    }

    /** Invoices a fulfilled order by handing it to Finance, then links the created invoice. */
    public SalesOrderResponse invoice(Long id) {
        SalesOrder order = load(id);
        switch (order.getStatus()) {
            case FULFILLED -> {
                SalesOrderInvoiceData data = new SalesOrderInvoiceData(
                        order.getId(), order.getCustomerId(),
                        order.getLines().stream()
                                .map(l -> new SalesOrderInvoiceData.Line(
                                        l.getDescription(), l.getQuantity(),
                                        l.getUnitPrice().getAmount(), l.getUnitPrice().getCurrency()))
                                .toList());
                order.setInvoiceId(invoices.createFromOrder(data));
                order.setStatus(OrderStatus.INVOICED);
            }
            case OPEN -> throw new InvalidStateException("Fulfill the order before invoicing");
            case INVOICED, CANCELLED ->
                    throw new InvalidStateException("Cannot invoice an order in status " + order.getStatus());
        }
        return toResponse(order);
    }

    /** Cancels an order that has not yet been fulfilled. */
    public SalesOrderResponse cancel(Long id) {
        SalesOrder order = load(id);
        switch (order.getStatus()) {
            case OPEN -> order.setStatus(OrderStatus.CANCELLED);
            case FULFILLED, INVOICED, CANCELLED ->
                    throw new InvalidStateException("Cannot cancel an order in status " + order.getStatus());
        }
        return toResponse(order);
    }

    /** All orders. */
    @Transactional(readOnly = true)
    public List<SalesOrderResponse> list() {
        return orders.findAll().stream().map(this::toResponse).toList();
    }

    /** Loads one order as a response, or throws 404. */
    @Transactional(readOnly = true)
    public SalesOrderResponse get(Long id) {
        return toResponse(load(id));
    }

    private SalesOrder load(Long id) {
        return orders.findById(id).orElseThrow(() -> new NotFoundException("Sales order", id));
    }

    /** Resolves the cross-module names and maps to the response. */
    private SalesOrderResponse toResponse(SalesOrder o) {
        String customerName = customers.get(o.getCustomerId()).getName();
        String ownerName = employees.get(o.getOwnerEmployeeId()).name();
        return SalesOrderResponse.from(o, customerName, ownerName);
    }
}
