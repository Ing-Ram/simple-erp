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
