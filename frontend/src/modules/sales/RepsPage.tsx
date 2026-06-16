import { useState } from "react";
import { Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { queryKeys } from "../../lib/queryKeys";
import { count, money, percent } from "../../lib/format";
import { PageHeader } from "../../components/PageHeader";
import { DashboardCard } from "../../components/DashboardCard";
import { DataTable } from "../../components/DataTable";
import { ErrorState } from "../../components/ErrorState";
import { EmptyState } from "../../components/EmptyState";
import { DashboardSkeleton } from "../../components/DashboardSkeleton";
import { fetchReps } from "./api";
import type { RepPeriod } from "./types";

const PERIODS: { value: RepPeriod; label: string }[] = [
  { value: "all", label: "All time" },
  { value: "quarter", label: "This quarter" },
  { value: "last90", label: "Last 90 days" },
];

/** Salesperson leaderboard: deals won, value, win rate, and open pipeline per rep. */
export function RepsPage() {
  const [period, setPeriod] = useState<RepPeriod>("all");

  const { data, isPending, isError, refetch } = useQuery({
    queryKey: queryKeys.sales.reps(period),
    queryFn: () => fetchReps(period),
  });

  return (
    <div className="space-y-6">
      <div className="flex items-end justify-between">
        <PageHeader module="Salespeople" asOf={new Date()} />
        <label className="text-sm">
          <span className="text-neutral-600">Won / lost in</span>
          <select
            value={period}
            onChange={(e) => setPeriod(e.target.value as RepPeriod)}
            className="mt-1 block w-44 rounded border border-neutral-300 px-2 py-1"
          >
            {PERIODS.map((p) => (
              <option key={p.value} value={p.value}>
                {p.label}
              </option>
            ))}
          </select>
        </label>
      </div>

      <DashboardCard title="Deal performance by salesperson">
        {isPending ? (
          <DashboardSkeleton />
        ) : isError ? (
          <ErrorState message="Salesperson performance didn't load." onRetry={refetch} />
        ) : data.length === 0 ? (
          <EmptyState
            title="No salespeople yet"
            action="Opportunities you create are owned by a salesperson and roll up here."
          />
        ) : (
          <DataTable
            rows={data}
            rowKey={(r) => r.employeeId}
            columns={[
              {
                header: "Salesperson",
                cell: (r) => (
                  <Link
                    to={`/sales/closed?owner=${r.employeeId}`}
                    className="font-medium text-sales hover:underline"
                  >
                    {r.name}
                  </Link>
                ),
              },
              { header: "Won", align: "right", cell: (r) => count(r.wonCount) },
              { header: "Won value", align: "right", cell: (r) => money(r.wonValue) },
              { header: "Avg deal", align: "right", cell: (r) => money(r.averageDealSize) },
              { header: "Lost", align: "right", cell: (r) => count(r.lostCount) },
              { header: "Win rate", align: "right", cell: (r) => percent(r.winRate) },
              { header: "Open pipeline", align: "right", cell: (r) => money(r.openPipelineWeighted) },
            ]}
          />
        )}
      </DashboardCard>
    </div>
  );
}
