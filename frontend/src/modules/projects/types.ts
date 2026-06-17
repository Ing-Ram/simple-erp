/** Mirrors backend ProjectsDashboardResponse field-for-field. */
export interface ProjectsDashboard {
  activeProjects: number;
  atRiskOrOverBudget: number;
  hoursThisWeek: number;
  utilization: number;
  budgetVsActual: ProjectBudgetRow[];
  upcomingMilestones: UpcomingMilestone[];
  needsAttention: ProjectAttentionRow[];
}

export type BudgetHealth = "ON_TRACK" | "AT_RISK" | "OVER";

export interface ProjectBudgetRow {
  projectId: number;
  name: string;
  budget: number;
  spent: number;
  percentConsumed: number;
  health: BudgetHealth;
}

export interface UpcomingMilestone {
  id: number;
  projectName: string;
  name: string;
  dueDate: string;
  overdue: boolean;
}

export interface ProjectAttentionRow {
  kind: "TASK" | "PROJECT";
  id: number;
  label: string;
  projectName: string;
  dueDate: string | null;
}
