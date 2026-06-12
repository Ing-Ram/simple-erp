package com.simpleerp.finance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.simpleerp.finance.dto.PaymentRequest;
import com.simpleerp.shared.InvalidStateException;
import com.simpleerp.shared.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for AR invoice state transitions and payment math. */
@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock private InvoiceRepository invoices;
    @Mock private PaymentRepository payments;
    @Mock private CustomerRepository customers;

    private InvoiceService service;
    private Invoice invoice;

    @BeforeEach
    void setUp() {
        service = new InvoiceService(invoices, payments, customers);
        invoice = new Invoice();
        Customer customer = new Customer();
        customer.setName("Test Customer");
        invoice.setCustomer(customer);
        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setDueDate(LocalDate.now().plusDays(30));
        InvoiceLine line = new InvoiceLine();
        line.setQuantity(new BigDecimal("2"));
        line.setUnitPrice(new Money(new BigDecimal("50.00"), "USD"));
        invoice.getLines().add(line);
        when(invoices.findById(1L)).thenReturn(Optional.of(invoice));
    }

    @Test
    void partialPaymentMovesInvoiceToPartiallyPaid() {
        when(payments.totalPaidForInvoice(1L)).thenReturn(BigDecimal.ZERO);
        when(payments.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.recordPayment(1L, payment("40.00"));

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PARTIALLY_PAID);
    }

    @Test
    void paymentOfFullOutstandingMovesInvoiceToPaid() {
        when(payments.totalPaidForInvoice(1L)).thenReturn(new BigDecimal("40.00"));
        when(payments.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.recordPayment(1L, payment("60.00"));

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
    }

    @Test
    void checkDetailsArePersistedWithAccountNumberMasked() {
        when(payments.totalPaidForInvoice(1L)).thenReturn(BigDecimal.ZERO);
        when(payments.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var request = new PaymentRequest(new BigDecimal("40.00"), "USD", LocalDate.now(),
                PaymentMethod.CHECK, "021000021", "123456789", "10472");
        service.recordPayment(1L, request);

        var saved = org.mockito.ArgumentCaptor.forClass(Payment.class);
        org.mockito.Mockito.verify(payments).save(saved.capture());
        assertThat(saved.getValue().getRoutingNumber()).isEqualTo("021000021");
        assertThat(saved.getValue().getCheckNumber()).isEqualTo("10472");
        // Only the last 3 digits are stored; the full account number never reaches the database.
        assertThat(saved.getValue().getAccountNumber()).isEqualTo("••••••789");
    }

    @Test
    void overpaymentIsRejected() {
        when(payments.totalPaidForInvoice(1L)).thenReturn(BigDecimal.ZERO);

        assertThatThrownBy(() -> service.recordPayment(1L, payment("150.00")))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void voidIsRejectedAfterMoneyApplied() {
        invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);

        assertThatThrownBy(() -> service.voidInvoice(1L))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void sendIsRejectedWhenNotDraft() {
        assertThatThrownBy(() -> service.send(1L)).isInstanceOf(InvalidStateException.class);
    }

    private PaymentRequest payment(String amount) {
        return new PaymentRequest(new BigDecimal(amount), "USD", LocalDate.now(),
                PaymentMethod.BANK_TRANSFER, null, null, null);
    }
}
