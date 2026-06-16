/** Central query keys so mutations can invalidate per module. */
export const queryKeys = {
  finance: {
    dashboard: ["finance", "dashboard"] as const,
    invoices: ["finance", "invoices"] as const,
    invoice: (id: number) => ["finance", "invoices", id] as const,
    invoicePayments: (id: number) => ["finance", "invoices", id, "payments"] as const,
    bills: ["finance", "bills"] as const,
    bill: (id: number) => ["finance", "bills", id] as const,
    billPayments: (id: number) => ["finance", "bills", id, "payments"] as const,
    customers: ["finance", "customers"] as const,
    vendors: ["finance", "vendors"] as const,
  },
  hr: {
    dashboard: ["hr", "dashboard"] as const,
    rollCall: ["hr", "roll-call"] as const,
    employees: ["hr", "employees"] as const,
  },
  sales: {
    dashboard: ["sales", "dashboard"] as const,
    reps: (period: string) => ["sales", "reps", period] as const,
    closedDeals: (ownerId: number | "all") => ["sales", "closed-deals", ownerId] as const,
  },
  projects: { dashboard: ["projects", "dashboard"] as const },
};
