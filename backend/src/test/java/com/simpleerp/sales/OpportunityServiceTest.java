package com.simpleerp.sales;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.simpleerp.finance.Customer;
import com.simpleerp.finance.CustomerService;
import com.simpleerp.hr.EmployeeService;
import com.simpleerp.hr.EmployeeStatus;
import com.simpleerp.hr.dto.EmployeeResponse;
import com.simpleerp.shared.InvalidStateException;
import com.simpleerp.shared.Money;
import com.simpleerp.shared.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for opportunity stage transitions: win (creates order), lose (needs reason), advance. */
@ExtendWith(MockitoExtension.class)
class OpportunityServiceTest {

    @Mock private OpportunityRepository opportunities;
    @Mock private SalesOrderService salesOrders;
    @Mock private CustomerService customers;
    @Mock private EmployeeService employees;

    private OpportunityService service;

    @BeforeEach
    void setUp() {
        service = new OpportunityService(opportunities, salesOrders, customers, employees);
    }

    @Test
    void winCreatesOrderInSameFlowAndMovesToWon() {
        Opportunity opportunity = open(OpportunityStage.NEGOTIATION);
        when(opportunities.findById(1L)).thenReturn(Optional.of(opportunity));
        when(salesOrders.createFromWonOpportunity(opportunity)).thenReturn(new SalesOrder());
        stubNames();

        var response = service.win(1L);

        assertThat(response.stage()).isEqualTo(OpportunityStage.WON);
        assertThat(opportunity.getClosedDate()).isNotNull();
        verify(salesOrders).createFromWonOpportunity(opportunity);
    }

    @Test
    void winRejectsAnAlreadyClosedOpportunity() {
        Opportunity opportunity = open(OpportunityStage.WON);
        when(opportunities.findById(1L)).thenReturn(Optional.of(opportunity));

        assertThatThrownBy(() -> service.win(1L)).isInstanceOf(InvalidStateException.class);
    }

    @Test
    void loseRequiresAReason() {
        assertThatThrownBy(() -> service.lose(1L, "  ")).isInstanceOf(ValidationException.class);
    }

    @Test
    void loseRecordsReasonAndPreviousStage() {
        Opportunity opportunity = open(OpportunityStage.PROPOSAL);
        when(opportunities.findById(1L)).thenReturn(Optional.of(opportunity));
        stubNames();

        var response = service.lose(1L, "Budget cut");

        assertThat(response.stage()).isEqualTo(OpportunityStage.LOST);
        assertThat(opportunity.getLostReason()).isEqualTo("Budget cut");
        assertThat(opportunity.getPreviousStage()).isEqualTo(OpportunityStage.PROPOSAL);
    }

    @Test
    void advanceRejectsMovingBackward() {
        Opportunity opportunity = open(OpportunityStage.PROPOSAL);
        when(opportunities.findById(1L)).thenReturn(Optional.of(opportunity));

        assertThatThrownBy(() -> service.advance(1L, OpportunityStage.QUALIFIED))
                .isInstanceOf(InvalidStateException.class);
    }

    private Opportunity open(OpportunityStage stage) {
        Opportunity opportunity = new Opportunity();
        opportunity.setCustomerId(1L);
        opportunity.setOwnerEmployeeId(4L);
        opportunity.setExpectedValue(new Money(new BigDecimal("50000.00"), "USD"));
        opportunity.setProbability(60);
        opportunity.setExpectedCloseDate(LocalDate.now().plusDays(30));
        opportunity.setStage(stage);
        return opportunity;
    }

    private void stubNames() {
        Customer customer = new Customer();
        customer.setName("Acme Corp");
        when(customers.get(any())).thenReturn(customer);
        when(employees.get(any())).thenReturn(new EmployeeResponse(
                4L, "Dan Wright", null, null, null, null, null, null, EmployeeStatus.ACTIVE));
    }
}
