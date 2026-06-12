package com.simpleerp.finance;

import com.simpleerp.finance.dto.BillPaymentRequest;
import com.simpleerp.finance.dto.BillPaymentResponse;
import com.simpleerp.finance.dto.BillRequest;
import com.simpleerp.finance.dto.BillResponse;
import com.simpleerp.shared.InvalidStateException;
import com.simpleerp.shared.Masking;
import com.simpleerp.shared.Money;
import com.simpleerp.shared.NotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Business rules for the AP bill lifecycle, payments, and aging — the AP mirror of InvoiceService. */
@Service
@Transactional
public class BillService {

    private final BillRepository bills;
    private final BillPaymentRepository payments;
    private final VendorRepository vendors;

    public BillService(BillRepository bills, BillPaymentRepository payments, VendorRepository vendors) {
        this.bills = bills;
        this.payments = payments;
        this.vendors = vendors;
    }

    /** Creates a DRAFT bill from the request payload. */
    public BillResponse create(BillRequest request) {
        Vendor vendor = vendors.findById(request.vendorId())
                .orElseThrow(() -> new NotFoundException("Vendor", request.vendorId()));
        Bill bill = new Bill();
        bill.setVendor(vendor);
        bill.setIssueDate(request.issueDate());
        bill.setDueDate(request.dueDate());
        request.lines().forEach(l -> {
            BillLine line = new BillLine();
            line.setBill(bill);
            line.setDescription(l.description());
            line.setQuantity(l.quantity());
            line.setUnitPrice(new Money(l.unitPrice(), l.currency()));
            bill.getLines().add(line);
        });
        return toResponse(bills.save(bill));
    }

    /** Marks a DRAFT bill as SENT (approved for payment); exhaustive switch flags new states. */
    public BillResponse send(Long id) {
        Bill bill = load(id);
        switch (bill.getStatus()) {
            case DRAFT -> bill.setStatus(BillStatus.SENT);
            case SENT, PARTIALLY_PAID, PAID, VOID ->
                    throw new InvalidStateException("Cannot send a bill in status " + bill.getStatus());
        }
        return toResponse(bill);
    }

    /** Voids a bill; only allowed before any money has been applied. */
    public BillResponse voidBill(Long id) {
        Bill bill = load(id);
        switch (bill.getStatus()) {
            case DRAFT, SENT -> bill.setStatus(BillStatus.VOID);
            case PARTIALLY_PAID, PAID, VOID ->
                    throw new InvalidStateException("Cannot void a bill in status " + bill.getStatus());
        }
        return toResponse(bill);
    }

    /**
     * Records a payment and recomputes status from the outstanding balance: zero remaining means
     * PAID, anything left means PARTIALLY_PAID. Payments above outstanding are rejected.
     */
    public BillResponse recordPayment(Long billId, BillPaymentRequest request) {
        Bill bill = load(billId);
        switch (bill.getStatus()) {
            case SENT, PARTIALLY_PAID -> {
                // Payment allowed in these states.
            }
            case DRAFT, PAID, VOID ->
                    throw new InvalidStateException("Cannot pay a bill in status " + bill.getStatus());
        }
        BigDecimal outstanding = bill.total().getAmount()
                .subtract(payments.totalPaidForBill(billId));
        if (request.amount().compareTo(outstanding) > 0) {
            throw new InvalidStateException(
                    "Payment of " + request.amount() + " exceeds outstanding balance of " + outstanding);
        }
        BillPayment payment = new BillPayment();
        payment.setBill(bill);
        payment.setAmount(new Money(request.amount(), request.currency()));
        payment.setPaymentDate(request.paymentDate());
        payment.setMethod(request.method());
        payment.setRoutingNumber(request.routingNumber());
        // Store the account number masked to its last 3 digits; never persist the full value.
        payment.setAccountNumber(Masking.maskExceptLast(request.accountNumber(), 3));
        payment.setCheckNumber(request.checkNumber());
        payments.save(payment);

        boolean fullyPaid = request.amount().compareTo(outstanding) == 0;
        bill.setStatus(fullyPaid ? BillStatus.PAID : BillStatus.PARTIALLY_PAID);
        // Paid-to-date is what was paid before plus this payment; no need to re-query.
        BigDecimal amountPaid = bill.total().getAmount().subtract(outstanding).add(request.amount());
        return BillResponse.from(bill, amountPaid, LocalDate.now());
    }

    /** A page of bills, most recently issued first. Mapped in-transaction to avoid lazy errors. */
    @Transactional(readOnly = true)
    public Page<BillResponse> list(Pageable pageable) {
        LocalDate today = LocalDate.now();
        Page<Bill> page = bills.findAll(pageable);
        List<Long> ids = page.map(Bill::getId).getContent();
        Map<Long, BigDecimal> paidByBill = ids.isEmpty()
                ? Map.of()
                : payments.paidTotals(ids).stream()
                        .collect(Collectors.toMap(PaidTotal::documentId, PaidTotal::paid));
        return page.map(b ->
                BillResponse.from(b, paidByBill.getOrDefault(b.getId(), BigDecimal.ZERO), today));
    }

    /** AP aging buckets as of today. */
    @Transactional(readOnly = true)
    public List<AgingBucket> apAging() {
        return bills.apAging(LocalDate.now());
    }

    /** Loads one bill as a response, or throws 404. */
    @Transactional(readOnly = true)
    public BillResponse get(Long id) {
        return toResponse(load(id));
    }

    /** Payment history for one bill, most recent first; 404 if the bill does not exist. */
    @Transactional(readOnly = true)
    public List<BillPaymentResponse> payments(Long billId) {
        load(billId);
        return payments.findByBill_IdOrderByPaymentDateDescIdDesc(billId).stream()
                .map(BillPaymentResponse::from)
                .toList();
    }

    /** Loads the bill entity or throws 404; for internal use within a transaction. */
    private Bill load(Long id) {
        return bills.findById(id).orElseThrow(() -> new NotFoundException("Bill", id));
    }

    /** Maps one bill to its response, pulling its paid-to-date total from the payment repository. */
    private BillResponse toResponse(Bill bill) {
        BigDecimal paid = payments.totalPaidForBill(bill.getId());
        return BillResponse.from(bill, paid, LocalDate.now());
    }
}
