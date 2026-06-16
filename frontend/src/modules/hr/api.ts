import { api } from "../../lib/api";
import type { HrDashboard, HrEmployee, RollCall, WorkMode } from "./types";

/** Typed endpoint functions for the HR module. */

export function fetchHrDashboard(): Promise<HrDashboard> {
  return api.get<HrDashboard>("/api/v1/hr/dashboard");
}

export function fetchEmployees(): Promise<HrEmployee[]> {
  return api.get<HrEmployee[]>("/api/v1/hr/employees");
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
