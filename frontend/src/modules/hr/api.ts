import { api } from "../../lib/api";
import type { HrDashboard } from "./types";

/** Typed endpoint functions for the HR module. */

export function fetchHrDashboard(): Promise<HrDashboard> {
  return api.get<HrDashboard>("/api/v1/hr/dashboard");
}

export function approveLeave(requestId: number): Promise<unknown> {
  return api.post<unknown>(`/api/v1/hr/leave-requests/${requestId}/approve`, {});
}

export function rejectLeave(requestId: number): Promise<unknown> {
  return api.post<unknown>(`/api/v1/hr/leave-requests/${requestId}/reject`, {});
}
