package com.simpleerp.finance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.simpleerp.finance.dto.BillPaymentRequest;
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

/** Unit tests for AP bill state transitions and payment math. */
@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock private BillRepository bills;
    @Mock private BillPaymentRepository payments;
    @Mock private VendorRepository vendors;

    private BillService service;
    private Bill bill;

    @BeforeEach
    void setUp() {
        service = new BillService(bills, payments, vendors);
        bill = new Bill();
        Vendor vendor = new Vendor();
        vendor.setName("Test Vendor");
        bill.setVendor(vendor);
        bill.setStatus(BillStatus.SENT);
        bill.setDueDate(LocalDate.now().plusDays(15));
        BillLine line = new BillLine();
        line.setQuantity(new BigDecimal("1"));
        line.setUnitPrice(new Money(new BigDecimal("200.00"), "USD"));
        bill.getLines().add(line);
        when(bills.findById(1L)).thenReturn(Optional.of(bill));
    }

    @Test
    void fullPaymentMovesBillToPaid() {
        when(payments.totalPaidForBill(1L)).thenReturn(BigDecimal.ZERO);
        when(payments.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.recordPayment(1L, payment("200.00"));

        assertThat(bill.getStatus()).isEqualTo(BillStatus.PAID);
    }

    @Test
    void overpaymentIsRejected() {
        when(payments.totalPaidForBill(1L)).thenReturn(BigDecimal.ZERO);

        assertThatThrownBy(() -> service.recordPayment(1L, payment("500.00")))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void voidIsRejectedAfterMoneyApplied() {
        bill.setStatus(BillStatus.PARTIALLY_PAID);

        assertThatThrownBy(() -> service.voidBill(1L)).isInstanceOf(InvalidStateException.class);
    }

    private BillPaymentRequest payment(String amount) {
        return new BillPaymentRequest(new BigDecimal(amount), "USD", LocalDate.now(),
                PaymentMethod.CARD, null, null, null);
    }
}
