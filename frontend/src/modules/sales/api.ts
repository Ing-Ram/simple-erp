import { api } from "../../lib/api";
import type { SalesDashboard } from "./types";

/** Typed endpoint functions for the Sales module. */
export function fetchSalesDashboard(): Promise<SalesDashboard> {
  return api.get<SalesDashboard>("/api/v1/sales/dashboard");
}
