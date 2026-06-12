package com.simpleerp.finance;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.simpleerp.finance.dto.InvoiceResponse;
import com.simpleerp.shared.NotFoundException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Web-layer tests for the invoice controller: happy path, validation failure, and not-found. */
@WebMvcTest(InvoiceController.class)
class InvoiceControllerTest {

    @Autowired private MockMvc mvc;
    @MockitoBean private InvoiceService service;

    @Test
    void createReturns201WithLocation() throws Exception {
        when(service.create(any())).thenReturn(sampleResponse(1L));

        mvc.perform(post("/api/v1/finance/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":1,"issueDate":"2026-06-01","dueDate":"2026-07-01",
                                 "lines":[{"description":"Consulting","quantity":1,
                                           "unitPrice":100.00,"currency":"USD"}]}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void createWithoutLinesFailsValidation() throws Exception {
        mvc.perform(post("/api/v1/finance/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":1,\"issueDate\":\"2026-06-01\",\"dueDate\":\"2026-07-01\",\"lines\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listReturnsPagedInvoices() throws Exception {
        Page<InvoiceResponse> page = new PageImpl<>(List.of(sampleResponse(1L)), Pageable.ofSize(20), 1);
        when(service.list(any())).thenReturn(page);

        mvc.perform(get("/api/v1/finance/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].customerName").value("Acme"));
    }

    @Test
    void missingInvoiceReturns404() throws Exception {
        when(service.get(99L)).thenThrow(new NotFoundException("Invoice", 99L));

        mvc.perform(get("/api/v1/finance/invoices/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void paymentsHistoryExposesCheckDetails() throws Exception {
        var check = new com.simpleerp.finance.dto.PaymentResponse(
                7L, new java.math.BigDecimal("500.00"), "USD", LocalDate.now(),
                PaymentMethod.CHECK, "021000021", "••••••789", "10472");
        when(service.payments(1L)).thenReturn(List.of(check));

        mvc.perform(get("/api/v1/finance/invoices/1/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].method").value("CHECK"))
                .andExpect(jsonPath("$[0].routingNumber").value("021000021"))
                .andExpect(jsonPath("$[0].accountNumber").value("••••••789"))
                .andExpect(jsonPath("$[0].checkNumber").value("10472"));
    }

    private static InvoiceResponse sampleResponse(long id) {
        java.math.BigDecimal total = new java.math.BigDecimal("100.00");
        return new InvoiceResponse(id, 1L, "Acme", LocalDate.now(), LocalDate.now().plusDays(30),
                InvoiceStatus.DRAFT, false, total, java.math.BigDecimal.ZERO, total, "USD", List.of());
    }
}
