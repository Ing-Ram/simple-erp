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
