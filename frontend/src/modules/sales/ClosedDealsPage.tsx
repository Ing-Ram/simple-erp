import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { queryKeys } from "../../lib/queryKeys";
import { money, shortDate } from "../../lib/format";
import { PageHeader } from "../../components/PageHeader";
import { DashboardCard } from "../../components/DashboardCard";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { ErrorState } from "../../components/ErrorState";
import { EmptyState } from "../../components/EmptyState";
import { DashboardSkeleton } from "../../components/DashboardSkeleton";
import { fetchClosedDeals, fetchReps } from "./api";

/** Closed deals (won and lost) with full detail, filterable by salesperson. */
export function ClosedDealsPage() {
  const [owner, setOwner] = useState<number | "all">("all");

  const { data: reps } = useQuery({ queryKey: queryKeys.sales.reps, queryFn: fetchReps });
  const { data, isPending, isError, refetch } = useQuery({
    queryKey: queryKeys.sales.closedDeals(owner),
    queryFn: () => fetchClosedDeals(owner === "all" ? undefined : owner),
  });

  return (
    <div className="space-y-6">
      <div className="flex items-end justify-between">
        <PageHeader module="Closed deals" asOf={new Date()} />
        <label className="text-sm">
          <span className="text-neutral-600">Salesperson</span>
          <select
            value={owner}
            onChange={(e) => setOwner(e.target.value === "all" ? "all" : Number(e.target.value))}
            className="mt-1 block w-56 rounded border border-neutral-300 px-2 py-1"
          >
            <option value="all">All salespeople</option>
            {reps?.map((r) => (
              <option key={r.employeeId} value={r.employeeId}>
                {r.name}
              </option>
            ))}
          </select>
        </label>
      </div>

      <DashboardCard title="Won and lost deals">
        {isPending ? (
          <DashboardSkeleton />
        ) : isError ? (
          <ErrorState message="Closed deals didn't load." onRetry={refetch} />
        ) : data.length === 0 ? (
          <EmptyState title="No closed deals" action="Won and lost opportunities appear here once decided." />
        ) : (
          <DataTable
            rows={data}
            rowKey={(d) => d.id}
            columns={[
              { header: "#", cell: (d) => d.id },
              { header: "Customer", cell: (d) => d.customerName },
              { header: "Salesperson", cell: (d) => d.ownerName },
              { header: "Outcome", cell: (d) => <StatusBadge status={d.stage} /> },
              { header: "Value", align: "right", cell: (d) => money(d.expectedValue, d.currency) },
              { header: "Closed", align: "right", cell: (d) => (d.closedDate ? shortDate(d.closedDate) : "—") },
              { header: "Reason / order", cell: (d) => dealNote(d.stage, d.lostReason, d.salesOrderId) },
            ]}
          />
        )}
      </DashboardCard>
    </div>
  );
}

/** Lost deals show their reason; won deals link their order number. */
function dealNote(stage: string, lostReason: string | null, salesOrderId: number | null) {
  if (stage === "LOST") return lostReason ?? "—";
  if (stage === "WON" && salesOrderId != null) return `Order #${salesOrderId}`;
  return "—";
}
