import { api } from "../../lib/api";
import type {
  Department,
  DepartmentRequest,
  EmployeeRequest,
  HrDashboard,
  HrEmployee,
  LeaveRequest,
  LeaveRequestRequest,
  RollCall,
  WorkMode,
} from "./types";

/** Typed endpoint functions for the HR module. */

export function fetchHrDashboard(): Promise<HrDashboard> {
  return api.get<HrDashboard>("/api/v1/hr/dashboard");
}

export function fetchEmployees(): Promise<HrEmployee[]> {
  return api.get<HrEmployee[]>("/api/v1/hr/employees");
}

export function createEmployee(body: EmployeeRequest): Promise<HrEmployee> {
  return api.post<HrEmployee>("/api/v1/hr/employees", body);
}

export function terminateEmployee(id: number, effectiveDate: string): Promise<HrEmployee> {
  return api.post<HrEmployee>(`/api/v1/hr/employees/${id}/terminate?effectiveDate=${effectiveDate}`, {});
}

export function fetchDepartments(): Promise<Department[]> {
  return api.get<Department[]>("/api/v1/hr/departments");
}

export function createDepartment(body: DepartmentRequest): Promise<Department> {
  return api.post<Department>("/api/v1/hr/departments", body);
}

export function fetchLeaveRequests(): Promise<LeaveRequest[]> {
  return api.get<LeaveRequest[]>("/api/v1/hr/leave-requests");
}

export function submitLeave(body: LeaveRequestRequest): Promise<LeaveRequest> {
  return api.post<LeaveRequest>("/api/v1/hr/leave-requests", body);
}

export function cancelLeave(id: number): Promise<LeaveRequest> {
  return api.post<LeaveRequest>(`/api/v1/hr/leave-requests/${id}/cancel`, {});
}

export function fetchRollCall(): Promise<RollCall> {
  return api.get<RollCall>("/api/v1/hr/presence/roll-call");
}

export function checkIn(employeeId: number, workMode: WorkMode): Promise<unknown> {
  return api.post<unknown>("/api/v1/hr/presence/check-in", { employeeId, workMode });
}

export function checkOut(employeeId: number): Promise<unknown> {
  return api.post<unknown>(`/api/v1/hr/presence/${employeeId}/check-out`, {});
}

export function approveLeave(requestId: number): Promise<unknown> {
  return api.post<unknown>(`/api/v1/hr/leave-requests/${requestId}/approve`, {});
}

export function rejectLeave(requestId: number): Promise<unknown> {
  return api.post<unknown>(`/api/v1/hr/leave-requests/${requestId}/reject`, {});
}
