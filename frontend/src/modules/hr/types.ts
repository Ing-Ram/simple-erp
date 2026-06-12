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
