import { api } from "../../lib/api";
import type {
  Bill,
  BillRequest,
  Customer,
  FinanceDashboard,
  Invoice,
  InvoiceRequest,
  Paged,
  PartyRequest,
  PaymentRecord,
  PaymentRequest,
  Vendor,
} from "./types";

/** Typed endpoint functions for the Finance module. */

export function fetchFinanceDashboard(): Promise<FinanceDashboard> {
  return api.get<FinanceDashboard>("/api/v1/finance/dashboard");
}

// Invoices (AR) ----------------------------------------------------------------

export function fetchInvoices(): Promise<Paged<Invoice>> {
  return api.get<Paged<Invoice>>("/api/v1/finance/invoices");
}

export function fetchInvoice(id: number): Promise<Invoice> {
  return api.get<Invoice>(`/api/v1/finance/invoices/${id}`);
}

export function fetchInvoicePayments(id: number): Promise<PaymentRecord[]> {
  return api.get<PaymentRecord[]>(`/api/v1/finance/invoices/${id}/payments`);
}

export function createInvoice(body: InvoiceRequest): Promise<Invoice> {
  return api.post<Invoice>("/api/v1/finance/invoices", body);
}

export function sendInvoice(id: number): Promise<Invoice> {
  return api.post<Invoice>(`/api/v1/finance/invoices/${id}/send`, {});
}

export function voidInvoice(id: number): Promise<Invoice> {
  return api.post<Invoice>(`/api/v1/finance/invoices/${id}/void`, {});
}

export function recordInvoicePayment(id: number, body: PaymentRequest): Promise<Invoice> {
  return api.post<Invoice>(`/api/v1/finance/invoices/${id}/payments`, body);
}

// Bills (AP) -------------------------------------------------------------------

export function fetchBills(): Promise<Paged<Bill>> {
  return api.get<Paged<Bill>>("/api/v1/finance/bills");
}

export function fetchBill(id: number): Promise<Bill> {
  return api.get<Bill>(`/api/v1/finance/bills/${id}`);
}

export function fetchBillPayments(id: number): Promise<PaymentRecord[]> {
  return api.get<PaymentRecord[]>(`/api/v1/finance/bills/${id}/payments`);
}

export function createBill(body: BillRequest): Promise<Bill> {
  return api.post<Bill>("/api/v1/finance/bills", body);
}

export function sendBill(id: number): Promise<Bill> {
  return api.post<Bill>(`/api/v1/finance/bills/${id}/send`, {});
}

export function voidBill(id: number): Promise<Bill> {
  return api.post<Bill>(`/api/v1/finance/bills/${id}/void`, {});
}

export function recordBillPayment(id: number, body: PaymentRequest): Promise<Bill> {
  return api.post<Bill>(`/api/v1/finance/bills/${id}/payments`, body);
}

// Parties ----------------------------------------------------------------------

export function fetchCustomers(): Promise<Customer[]> {
  return api.get<Customer[]>("/api/v1/finance/customers");
}

export function createCustomer(body: PartyRequest): Promise<Customer> {
  return api.post<Customer>("/api/v1/finance/customers", body);
}

export function fetchVendors(): Promise<Vendor[]> {
  return api.get<Vendor[]>("/api/v1/finance/vendors");
}

export function createVendor(body: PartyRequest): Promise<Vendor> {
  return api.post<Vendor>("/api/v1/finance/vendors", body);
}
