package com.simpleerp.sales;

import static org.assertj.core.api.Assertions.assertThat;

import com.simpleerp.finance.CustomerService;
import com.simpleerp.finance.InvoiceService;
import com.simpleerp.finance.InvoiceStatus;
import com.simpleerp.finance.dto.CustomerRequest;
import com.simpleerp.hr.DepartmentService;
import com.simpleerp.hr.EmployeeService;
import com.simpleerp.hr.dto.DepartmentRequest;
import com.simpleerp.hr.dto.EmployeeRequest;
import com.simpleerp.sales.dto.OpportunityRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Drives the win → order → invoice flow end to end against H2, crossing module boundaries: Sales
 * wins an opportunity (creating the order), then invoicing the fulfilled order hands off to Finance,
 * which raises a SENT AR invoice linked back to the order.
 */
@SpringBootTest
@Transactional
class SalesFlowSmokeTest {

    @Autowired private CustomerService customers;
    @Autowired private DepartmentService departments;
    @Autowired private EmployeeService employees;
    @Autowired private InvoiceService invoices;
    @Autowired private OpportunityService opportunities;
    @Autowired private SalesOrderService orders;
    @Autowired private SalesDashboardService dashboard;

    @Test
    void winningThenInvoicingAnOrderCreatesAnArInvoice() {
        Long customerId = customers.create(new CustomerRequest("Acme Corp", "ap@acme.example", 30)).getId();
        Long deptId = departments.create(new DepartmentRequest("Sales", null)).id();
        Long ownerId = employees.create(new EmployeeRequest(
                "Dan Wright", "dan@simpleerp.example", deptId, "Account Executive",
                LocalDate.now().minusMonths(6), new BigDecimal("110000.00"), "USD")).id();

        var opp = opportunities.create(new OpportunityRequest(
                customerId, ownerId, new BigDecimal("50000.00"), "USD", 70, LocalDate.now().plusDays(10)));

        // Winning creates the order in the same transaction.
        var won = opportunities.win(opp.id());
        assertThat(won.stage()).isEqualTo(OpportunityStage.WON);
        assertThat(won.salesOrderId()).isNotNull();
        Long orderId = won.salesOrderId();

        // Fulfil, then invoice — the hand-off to Finance.
        orders.fulfill(orderId);
        var invoiced = orders.invoice(orderId);
        assertThat(invoiced.status()).isEqualTo(OrderStatus.INVOICED);
        assertThat(invoiced.invoiceId()).isNotNull();

        // Finance now has a SENT invoice for the order's value, linked back to it.
        var invoice = invoices.get(invoiced.invoiceId());
        assertThat(invoice.status()).isEqualTo(InvoiceStatus.SENT);
        assertThat(invoice.total()).isEqualByComparingTo("50000.00");

        // The won deal shows up in this quarter's number.
        assertThat(dashboard.summary().wonThisQuarter()).isEqualByComparingTo("50000.00");
    }
}
