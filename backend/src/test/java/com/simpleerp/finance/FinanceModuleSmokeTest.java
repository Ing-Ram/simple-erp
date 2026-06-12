package com.simpleerp.finance;

import static org.assertj.core.api.Assertions.assertThat;

import com.simpleerp.finance.dto.InvoiceRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Boots the full context against H2 so Flyway migrations actually apply, then exercises the
 * dashboard aggregation stack end-to-end (native AR/AP aging plus the JPQL needs-attention
 * projections) both on an empty schema and with a real overdue invoice present.
 *
 * <p>{@code @Transactional} rolls back each method so the shared in-memory DB stays isolated.
 */
@SpringBootTest
@Transactional
class FinanceModuleSmokeTest {

    @Autowired private FinanceDashboardService dashboard;
    @Autowired private InvoiceService invoiceService;
    @Autowired private CustomerRepository customers;

    @Test
    void migrationsApplyAndDashboardAggregatesOnEmptyData() {
        var summary = dashboard.summary();

        assertThat(summary.asOf()).isNotNull();
        assertThat(summary.arOutstanding()).isNotNull();
        assertThat(summary.apOutstanding()).isNotNull();
        assertThat(summary.netPosition()).isNotNull();
        assertThat(summary.arAging()).isEmpty();
        assertThat(summary.apAging()).isEmpty();
        assertThat(summary.needsAttention()).isEmpty();
    }

    @Test
    void overdueInvoiceSurfacesInNeedsAttentionWithRealDate() {
        Customer customer = new Customer();
        customer.setName("Smoke Test Co");
        customer.setPaymentTermsDays(30);
        Long customerId = customers.save(customer).getId();

        var request = new InvoiceRequest(
                customerId,
                LocalDate.now().minusDays(45),
                LocalDate.now().minusDays(15),
                List.of(new InvoiceRequest.LineItemRequest(
                        "Overdue work", new BigDecimal("1"), new BigDecimal("500.00"), "USD")));
        Long invoiceId = invoiceService.create(request).id();
        invoiceService.send(invoiceId);

        var summary = dashboard.summary();

        assertThat(summary.overdueArCount()).isEqualTo(1);
        assertThat(summary.needsAttention()).hasSize(1);
        OverdueDocument row = summary.needsAttention().get(0);
        assertThat(row.kind()).isEqualTo("AR");
        assertThat(row.dueDate()).isEqualTo(LocalDate.now().minusDays(15));
        assertThat(row.outstanding()).isEqualByComparingTo("500.00");
    }
}
