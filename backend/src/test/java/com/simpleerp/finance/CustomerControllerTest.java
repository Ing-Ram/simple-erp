package com.simpleerp.finance;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Web-layer tests for the customer controller: list, create happy path, and validation failure. */
@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false) // exercise controller logic without the JWT security filter
class CustomerControllerTest {

    @Autowired private MockMvc mvc;
    @MockitoBean private CustomerService service;
    // The security filter is disabled here, but its bean still needs its collaborators present.
    @MockitoBean private com.simpleerp.auth.JwtService jwtService;
    @MockitoBean private com.simpleerp.auth.AppUserDetailsService appUserDetailsService;

    @Test
    void listReturnsCustomers() throws Exception {
        Customer customer = new Customer();
        customer.setName("Acme Corp");
        customer.setPaymentTermsDays(30);
        when(service.list()).thenReturn(List.of(customer));

        mvc.perform(get("/api/v1/finance/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Acme Corp"));
    }

    @Test
    void createReturns201() throws Exception {
        Customer saved = new Customer();
        saved.setName("New Co");
        saved.setPaymentTermsDays(30);
        when(service.create(any())).thenReturn(saved);

        mvc.perform(post("/api/v1/finance/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Co\",\"email\":\"ap@newco.example\",\"paymentTermsDays\":30}"))
                .andExpect(status().isCreated());
    }

    @Test
    void createWithBlankNameFailsValidation() throws Exception {
        mvc.perform(post("/api/v1/finance/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"ap@newco.example\",\"paymentTermsDays\":30}"))
                .andExpect(status().isBadRequest());
    }
}
