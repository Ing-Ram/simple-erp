/** Mirrors backend SalesDashboardResponse field-for-field. */
export interface SalesDashboard {
  openPipelineWeighted: number;
  wonThisQuarter: number;
  winRateLast90Days: number;
  averageDealSize: number;
  funnel: StageFunnel[];
  monthlyWon: MonthlyWon[];
  needsAttention: NeedsAttentionRow[];
}

export interface StageFunnel {
  stage: string;
  count: number;
  value: number;
}

export interface MonthlyWon {
  month: string;
  amount: number;
}

export interface NeedsAttentionRow {
  kind: "OPPORTUNITY" | "ORDER";
  id: number;
  customerName: string;
  amount: number;
  date: string;
}

export type OpportunityStage =
  | "PROSPECTING"
  | "QUALIFIED"
  | "PROPOSAL"
  | "NEGOTIATION"
  | "WON"
  | "LOST";

export type LeadSource = "REFERRAL" | "WEBSITE" | "OUTBOUND" | "EVENT";
export type LeadStatus = "NEW" | "CONTACTED" | "QUALIFIED" | "DISQUALIFIED";
export type OrderStatus = "OPEN" | "FULFILLED" | "INVOICED" | "CANCELLED";

/** Mirrors backend LeadResponse. */
export interface Lead {
  id: number;
  name: string;
  company: string | null;
  email: string | null;
  source: LeadSource;
  status: LeadStatus;
  customerId: number | null;
  opportunityId: number | null;
}

/** Mirrors backend SalesOrderResponse. */
export interface SalesOrder {
  id: number;
  customerId: number;
  customerName: string;
  ownerEmployeeId: number;
  ownerName: string;
  status: OrderStatus;
  orderDate: string;
  opportunityId: number | null;
  invoiceId: number | null;
  total: number;
  currency: string;
  lines: { id: number; description: string; quantity: number; unitPrice: number }[];
}

/** Payload for POST /sales/leads. */
export interface LeadRequest {
  name: string;
  company: string;
  email: string;
  source: LeadSource;
}

/** Payload for qualifying a lead. */
export interface QualifyLeadRequest {
  ownerEmployeeId: number;
  expectedValue: number;
  currency: string;
  probability: number;
  expectedCloseDate: string;
  paymentTermsDays: number;
}

/** Payload for POST /sales/opportunities. */
export interface OpportunityRequest {
  customerId: number;
  ownerEmployeeId: number;
  expectedValue: number;
  currency: string;
  probability: number;
  expectedCloseDate: string;
}

/** Mirrors backend OpportunityResponse (used for the closed-deals list). */
export interface Opportunity {
  id: number;
  customerId: number;
  customerName: string;
  ownerEmployeeId: number;
  ownerName: string;
  expectedValue: number;
  weightedValue: number;
  currency: string;
  probability: number;
  expectedCloseDate: string;
  stage: OpportunityStage;
  lostReason: string | null;
  closedDate: string | null;
  salesOrderId: number | null;
}

/** Period scope for the salesperson leaderboard's won/lost figures. */
export type RepPeriod = "all" | "quarter" | "last90";

/** Mirrors backend RepPerformanceResponse. */
export interface RepPerformance {
  employeeId: number;
  name: string;
  wonCount: number;
  wonValue: number;
  averageDealSize: number;
  lostCount: number;
  winRate: number;
  openPipelineWeighted: number;
}
