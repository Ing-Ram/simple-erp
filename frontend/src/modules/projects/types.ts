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

export type ProjectStatus = "PLANNED" | "ACTIVE" | "ON_HOLD" | "COMPLETED" | "CANCELLED";
export type TaskStatus = "TODO" | "IN_PROGRESS" | "DONE";

/** Mirrors backend ProjectResponse. */
export interface Project {
  id: number;
  name: string;
  customerId: number | null;
  customerName: string | null;
  managerEmployeeId: number;
  managerName: string;
  startDate: string | null;
  targetEndDate: string | null;
  budget: number;
  spent: number;
  percentConsumed: number;
  budgetHealth: BudgetHealth;
  currency: string;
  status: ProjectStatus;
}

/** Mirrors backend TaskResponse. */
export interface Task {
  id: number;
  projectId: number;
  title: string;
  assigneeEmployeeId: number | null;
  assigneeName: string | null;
  status: TaskStatus;
  dueDate: string | null;
  estimateHours: number | null;
}

/** Mirrors backend MilestoneResponse. */
export interface Milestone {
  id: number;
  projectId: number;
  name: string;
  dueDate: string | null;
  completedAt: string | null;
  waived: boolean;
  resolved: boolean;
}

/** Mirrors backend TimeEntryResponse. */
export interface TimeEntry {
  id: number;
  taskId: number;
  taskTitle: string;
  employeeId: number;
  employeeName: string;
  entryDate: string;
  hours: number;
  note: string | null;
}

/** Payload for POST /projects. */
export interface ProjectRequest {
  name: string;
  customerId: number | null;
  managerEmployeeId: number;
  startDate: string;
  targetEndDate: string;
  budget: number;
  currency: string;
}

/** Payload for POST /projects/tasks. */
export interface TaskRequest {
  projectId: number;
  title: string;
  assigneeEmployeeId: number | null;
  dueDate: string | null;
  estimateHours: number | null;
}

/** Payload for POST /projects/milestones. */
export interface MilestoneRequest {
  projectId: number;
  name: string;
  dueDate: string | null;
}

/** Payload for POST /projects/time-entries. */
export interface TimeEntryRequest {
  taskId: number;
  employeeId: number;
  entryDate: string;
  hours: number;
  note: string | null;
}
