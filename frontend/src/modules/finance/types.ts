/** Mirrors backend FinanceDashboardResponse field-for-field. Keep in sync with the record. */
export interface FinanceDashboard {
  asOf: string;
  arOutstanding: number;
  apOutstanding: number;
  overdueArCount: number;
  overdueArAmount: number;
  netPosition: number;
  cashInLast30Days: number;
  cashOutLast30Days: number;
  arAging: AgingBucket[];
  apAging: AgingBucket[];
  needsAttention: OverdueDocument[];
}

/** One aging-bucket row. Bucket codes: CURRENT, D1_30, D31_60, D61_90, D90_PLUS. */
export interface AgingBucket {
  bucket: string;
  documentCount: number;
  outstanding: number;
}

/** A "needs attention" row: an overdue AR invoice (kind "AR") or a soon-due AP bill (kind "AP"). */
export interface OverdueDocument {
  kind: "AR" | "AP";
  documentId: number;
  party: string;
  dueDate: string;
  outstanding: number;
}

/** Document lifecycle states shared by invoices and bills. */
export type DocumentStatus = "DRAFT" | "SENT" | "PARTIALLY_PAID" | "PAID" | "VOID";

export type PaymentMethod = "BANK_TRANSFER" | "CARD" | "CHECK" | "CASH";

/** Mirrors backend InvoiceResponse / BillResponse line shape. */
export interface DocumentLine {
  id: number;
  description: string;
  quantity: number;
  unitPrice: number;
}

/** Mirrors backend InvoiceResponse. */
export interface Invoice {
  id: number;
  customerId: number;
  customerName: string;
  issueDate: string;
  dueDate: string;
  status: DocumentStatus;
  overdue: boolean;
  total: number;
  amountPaid: number;
  outstanding: number;
  currency: string;
  lines: DocumentLine[];
}

/** Mirrors backend BillResponse. */
export interface Bill {
  id: number;
  vendorId: number;
  vendorName: string;
  issueDate: string;
  dueDate: string;
  status: DocumentStatus;
  overdue: boolean;
  total: number;
  amountPaid: number;
  outstanding: number;
  currency: string;
  lines: DocumentLine[];
}

/** Mirrors backend CustomerResponse. */
export interface Customer {
  id: number;
  name: string;
  email: string | null;
  paymentTermsDays: number;
  active: boolean;
}

/** Payload for creating a customer or vendor (CustomerRequest / VendorRequest share a shape). */
export interface PartyRequest {
  name: string;
  email: string;
  paymentTermsDays: number;
}

/** Mirrors backend VendorResponse. */
export interface Vendor {
  id: number;
  name: string;
  email: string | null;
  paymentTermsDays: number;
  active: boolean;
}

/** Mirrors backend PagedResponse<T>. */
export interface Paged<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/** A line on a create-document request. */
export interface LineItemRequest {
  description: string;
  quantity: number;
  unitPrice: number;
  currency: string;
}

/** Payload for POST /invoices. */
export interface InvoiceRequest {
  customerId: number;
  issueDate: string;
  dueDate: string;
  lines: LineItemRequest[];
}

/** Payload for POST /bills. */
export interface BillRequest {
  vendorId: number;
  issueDate: string;
  dueDate: string;
  lines: LineItemRequest[];
}

/** A recorded payment as returned by the payment-history endpoints. */
export interface PaymentRecord {
  id: number;
  amount: number;
  currency: string;
  paymentDate: string;
  method: PaymentMethod;
  routingNumber: string | null;
  /** Already masked to its last 3 digits by the backend. */
  accountNumber: string | null;
  checkNumber: string | null;
}

/** Payload for recording a payment against an invoice or bill. */
export interface PaymentRequest {
  amount: number;
  currency: string;
  paymentDate: string;
  method: PaymentMethod;
  /** Optional; only sent for CHECK payments. The account number is sent in full and masked server-side. */
  routingNumber?: string;
  accountNumber?: string;
  checkNumber?: string;
}
