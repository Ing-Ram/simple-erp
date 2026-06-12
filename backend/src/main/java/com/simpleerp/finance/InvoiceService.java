package com.simpleerp.finance;

import com.simpleerp.finance.dto.InvoiceRequest;
import com.simpleerp.finance.dto.InvoiceResponse;
import com.simpleerp.finance.dto.PaymentRequest;
import com.simpleerp.finance.dto.PaymentResponse;
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

/** Business rules for the AR invoice lifecycle, payments, and aging. */
@Service
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoices;
    private final PaymentRepository payments;
    private final CustomerRepository customers;

    public InvoiceService(InvoiceRepository invoices, PaymentRepository payments,
                          CustomerRepository customers) {
        this.invoices = invoices;
        this.payments = payments;
        this.customers = customers;
    }

    /** Creates a DRAFT invoice from the request payload. */
    public InvoiceResponse create(InvoiceRequest request) {
        Customer customer = customers.findById(request.customerId())
                .orElseThrow(() -> new NotFoundException("Customer", request.customerId()));
        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setIssueDate(request.issueDate());
        invoice.setDueDate(request.dueDate());
        request.lines().forEach(l -> {
            InvoiceLine line = new InvoiceLine();
            line.setInvoice(invoice);
            line.setDescription(l.description());
            line.setQuantity(l.quantity());
            line.setUnitPrice(new Money(l.unitPrice(), l.currency()));
            invoice.getLines().add(line);
        });
        return toResponse(invoices.save(invoice));
    }

    /** Marks a DRAFT invoice as SENT; the exhaustive switch makes new states a compile error. */
    public InvoiceResponse send(Long id) {
        Invoice invoice = load(id);
        switch (invoice.getStatus()) {
            case DRAFT -> invoice.setStatus(InvoiceStatus.SENT);
            case SENT, PARTIALLY_PAID, PAID, VOID ->
                    throw new InvalidStateException("Cannot send an invoice in status " + invoice.getStatus());
        }
        return toResponse(invoice);
    }

    /** Voids an invoice; only allowed before any money has been applied. */
    public InvoiceResponse voidInvoice(Long id) {
        Invoice invoice = load(id);
        switch (invoice.getStatus()) {
            case DRAFT, SENT -> invoice.setStatus(InvoiceStatus.VOID);
            case PARTIALLY_PAID, PAID, VOID ->
                    throw new InvalidStateException("Cannot void an invoice in status " + invoice.getStatus());
        }
        return toResponse(invoice);
    }

    /**
     * Records a payment and recomputes status from the outstanding balance: a zero remaining
     * balance means PAID, anything left means PARTIALLY_PAID. Payments above outstanding are rejected.
     */
    public InvoiceResponse recordPayment(Long invoiceId, PaymentRequest request) {
        Invoice invoice = load(invoiceId);
        switch (invoice.getStatus()) {
            case SENT, PARTIALLY_PAID -> {
                // Payment allowed in these states.
            }
            case DRAFT, PAID, VOID ->
                    throw new InvalidStateException("Cannot pay an invoice in status " + invoice.getStatus());
        }
        BigDecimal outstanding = invoice.total().getAmount()
                .subtract(payments.totalPaidForInvoice(invoiceId));
        if (request.amount().compareTo(outstanding) > 0) {
            throw new InvalidStateException(
                    "Payment of " + request.amount() + " exceeds outstanding balance of " + outstanding);
        }
        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(new Money(request.amount(), request.currency()));
        payment.setPaymentDate(request.paymentDate());
        payment.setMethod(request.method());
        payment.setRoutingNumber(request.routingNumber());
        // Store the account number masked to its last 3 digits; never persist the full value.
        payment.setAccountNumber(Masking.maskExceptLast(request.accountNumber(), 3));
        payment.setCheckNumber(request.checkNumber());
        payments.save(payment);

        boolean fullyPaid = request.amount().compareTo(outstanding) == 0;
        invoice.setStatus(fullyPaid ? InvoiceStatus.PAID : InvoiceStatus.PARTIALLY_PAID);
        // Paid-to-date is what was paid before plus this payment; no need to re-query.
        BigDecimal amountPaid = invoice.total().getAmount().subtract(outstanding).add(request.amount());
        return InvoiceResponse.from(invoice, amountPaid, LocalDate.now());
    }

    /** A page of invoices, most recently issued first. Mapped in-transaction to avoid lazy errors. */
    @Transactional(readOnly = true)
    public Page<InvoiceResponse> list(Pageable pageable) {
        LocalDate today = LocalDate.now();
        Page<Invoice> page = invoices.findAll(pageable);
        List<Long> ids = page.map(Invoice::getId).getContent();
        Map<Long, BigDecimal> paidByInvoice = ids.isEmpty()
                ? Map.of()
                : payments.paidTotals(ids).stream()
                        .collect(Collectors.toMap(PaidTotal::documentId, PaidTotal::paid));
        return page.map(i ->
                InvoiceResponse.from(i, paidByInvoice.getOrDefault(i.getId(), BigDecimal.ZERO), today));
    }

    /** AR aging buckets as of today. */
    @Transactional(readOnly = true)
    public List<AgingBucket> arAging() {
        return invoices.arAging(LocalDate.now());
    }

    /** Loads one invoice as a response, or throws 404. */
    @Transactional(readOnly = true)
    public InvoiceResponse get(Long id) {
        return toResponse(load(id));
    }

    /** Payment history for one invoice, most recent first; 404 if the invoice does not exist. */
    @Transactional(readOnly = true)
    public List<PaymentResponse> payments(Long invoiceId) {
        load(invoiceId);
        return payments.findByInvoice_IdOrderByPaymentDateDescIdDesc(invoiceId).stream()
                .map(PaymentResponse::from)
                .toList();
    }

    /** Loads the invoice entity or throws 404; for internal use within a transaction. */
    private Invoice load(Long id) {
        return invoices.findById(id).orElseThrow(() -> new NotFoundException("Invoice", id));
    }

    /** Maps one invoice to its response, pulling its paid-to-date total from the payment repository. */
    private InvoiceResponse toResponse(Invoice invoice) {
        BigDecimal paid = payments.totalPaidForInvoice(invoice.getId());
        return InvoiceResponse.from(invoice, paid, LocalDate.now());
    }
}
