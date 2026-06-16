import { api } from "../../lib/api";
import type { Opportunity, RepPerformance, SalesDashboard } from "./types";

/** Typed endpoint functions for the Sales module. */
export function fetchSalesDashboard(): Promise<SalesDashboard> {
  return api.get<SalesDashboard>("/api/v1/sales/dashboard");
}

export function fetchReps(): Promise<RepPerformance[]> {
  return api.get<RepPerformance[]>("/api/v1/sales/reps");
}

export function fetchClosedDeals(ownerEmployeeId?: number): Promise<Opportunity[]> {
  const query = ownerEmployeeId == null ? "" : `?ownerEmployeeId=${ownerEmployeeId}`;
  return api.get<Opportunity[]>(`/api/v1/sales/opportunities/closed${query}`);
}
