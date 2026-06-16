import { api } from "../../lib/api";
import type {
  Lead,
  LeadRequest,
  Opportunity,
  OpportunityRequest,
  OpportunityStage,
  QualifyLeadRequest,
  RepPerformance,
  RepPeriod,
  SalesDashboard,
  SalesOrder,
} from "./types";

/** Typed endpoint functions for the Sales module. */
export function fetchSalesDashboard(): Promise<SalesDashboard> {
  return api.get<SalesDashboard>("/api/v1/sales/dashboard");
}

// Leads ------------------------------------------------------------------------

export function fetchLeads(): Promise<Lead[]> {
  return api.get<Lead[]>("/api/v1/sales/leads");
}

export function createLead(body: LeadRequest): Promise<Lead> {
  return api.post<Lead>("/api/v1/sales/leads", body);
}

export function contactLead(id: number): Promise<Lead> {
  return api.post<Lead>(`/api/v1/sales/leads/${id}/contact`, {});
}

export function qualifyLead(id: number, body: QualifyLeadRequest): Promise<Lead> {
  return api.post<Lead>(`/api/v1/sales/leads/${id}/qualify`, body);
}

export function disqualifyLead(id: number): Promise<Lead> {
  return api.post<Lead>(`/api/v1/sales/leads/${id}/disqualify`, {});
}

// Opportunities ----------------------------------------------------------------

export function fetchOpportunities(): Promise<Opportunity[]> {
  return api.get<Opportunity[]>("/api/v1/sales/opportunities");
}

export function createOpportunity(body: OpportunityRequest): Promise<Opportunity> {
  return api.post<Opportunity>("/api/v1/sales/opportunities", body);
}

export function advanceOpportunity(id: number, stage: OpportunityStage): Promise<Opportunity> {
  return api.post<Opportunity>(`/api/v1/sales/opportunities/${id}/advance`, { stage });
}

export function winOpportunity(id: number): Promise<Opportunity> {
  return api.post<Opportunity>(`/api/v1/sales/opportunities/${id}/win`, {});
}

export function loseOpportunity(id: number, lostReason: string): Promise<Opportunity> {
  return api.post<Opportunity>(`/api/v1/sales/opportunities/${id}/lose`, { lostReason });
}

export function reopenOpportunity(id: number): Promise<Opportunity> {
  return api.post<Opportunity>(`/api/v1/sales/opportunities/${id}/reopen`, {});
}

// Orders -----------------------------------------------------------------------

export function fetchOrders(): Promise<SalesOrder[]> {
  return api.get<SalesOrder[]>("/api/v1/sales/orders");
}

export function fulfillOrder(id: number): Promise<SalesOrder> {
  return api.post<SalesOrder>(`/api/v1/sales/orders/${id}/fulfill`, {});
}

export function invoiceOrder(id: number): Promise<SalesOrder> {
  return api.post<SalesOrder>(`/api/v1/sales/orders/${id}/invoice`, {});
}

export function cancelOrder(id: number): Promise<SalesOrder> {
  return api.post<SalesOrder>(`/api/v1/sales/orders/${id}/cancel`, {});
}

export function fetchReps(period: RepPeriod = "all"): Promise<RepPerformance[]> {
  return api.get<RepPerformance[]>(`/api/v1/sales/reps?period=${period}`);
}

export function fetchClosedDeals(ownerEmployeeId?: number): Promise<Opportunity[]> {
  const query = ownerEmployeeId == null ? "" : `?ownerEmployeeId=${ownerEmployeeId}`;
  return api.get<Opportunity[]>(`/api/v1/sales/opportunities/closed${query}`);
}
