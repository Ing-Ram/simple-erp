/** Mirrors backend HrDashboardResponse field-for-field. */
export interface HrDashboard {
  activeHeadcount: number;
  hiresLast90Days: number;
  pendingLeaveRequests: number;
  turnoverRate: number;
  headcountByDepartment: DepartmentHeadcount[];
  whosOut: OutToday[];
  needsAttention: PendingLeave[];
}

export interface DepartmentHeadcount {
  department: string;
  headcount: number;
}

export interface OutToday {
  employeeId: number;
  name: string;
  leaveType: string;
  startDate: string;
  endDate: string;
}

export interface PendingLeave {
  requestId: number;
  employeeId: number;
  name: string;
  leaveType: string;
  startDate: string;
  endDate: string;
  requestedOn: string;
}

export type WorkMode = "ON_SITE" | "REMOTE";

export type AccountabilityStatus =
  | "PRESENT"
  | "CHECKED_OUT"
  | "REMOTE"
  | "ON_LEAVE"
  | "UNACCOUNTED";

/** Mirrors backend EmployeeResponse (used for the check-in selector). */
export interface HrEmployee {
  id: number;
  name: string;
  email: string | null;
  departmentId: number | null;
  departmentName: string | null;
  position: string | null;
  hireDate: string;
  terminationDate: string | null;
  status: "ACTIVE" | "ON_LEAVE" | "TERMINATED";
}

/** Mirrors backend RollCallEntry. */
export interface RollCallEntry {
  employeeId: number;
  name: string;
  department: string | null;
  status: AccountabilityStatus;
  since: string | null;
}

/** Mirrors backend RollCallResponse. */
export interface RollCall {
  asOf: string;
  presentCount: number;
  checkedOutCount: number;
  remoteCount: number;
  onLeaveCount: number;
  unaccountedCount: number;
  entries: RollCallEntry[];
}
