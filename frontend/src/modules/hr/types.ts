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

export type LeaveType = "VACATION" | "SICK" | "UNPAID" | "PARENTAL";
export type LeaveStatus = "PENDING" | "APPROVED" | "REJECTED" | "CANCELLED";

/** Mirrors backend DepartmentResponse. */
export interface Department {
  id: number;
  name: string;
  managerId: number | null;
  managerName: string | null;
}

/** Mirrors backend LeaveRequestResponse. */
export interface LeaveRequest {
  id: number;
  employeeId: number;
  employeeName: string;
  type: LeaveType;
  startDate: string;
  endDate: string;
  businessDays: number;
  status: LeaveStatus;
  reviewer: string | null;
  decidedAt: string | null;
}

/** Payload for hiring an employee. */
export interface EmployeeRequest {
  name: string;
  email: string;
  departmentId: number;
  position: string;
  hireDate: string;
  salary: number;
  currency: string;
}

/** Payload for creating a department. */
export interface DepartmentRequest {
  name: string;
  managerId: number | null;
}

/** Payload for submitting a leave request. */
export interface LeaveRequestRequest {
  employeeId: number;
  type: LeaveType;
  startDate: string;
  endDate: string;
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
