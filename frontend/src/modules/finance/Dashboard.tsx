import { useQuery } from "@tanstack/react-query";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { queryKeys } from "../../lib/queryKeys";
import { money, shortDate } from "../../lib/format";
import { moduleAccent, semantic } from "../../lib/tokens";
import { PageHeader } from "../../components/PageHeader";
import { StatCard } from "../../components/StatCard";
import { DashboardCard } from "../../components/DashboardCard";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { ErrorState } from "../../components/ErrorState";
import { EmptyState } from "../../components/EmptyState";
import { DashboardSkeleton } from "../../components/DashboardSkeleton";
import { fetchFinanceDashboard } from "./api";
import type { AgingBucket } from "./types";

/** Human labels for the fixed aging-bucket order. */
const BUCKETS: { code: string; label: string }[] = [
  { code: "CURRENT", label: "Current" },
  { code: "D1_30", label: "1–30" },
  { code: "D31_60", label: "31–60" },
  { code: "D61_90", label: "61–90" },
  { code: "D90_PLUS", label: "90+" },
];

/** Merges AR and AP aging rows into one chart dataset keyed by bucket, in fixed order. */
function agingSeries(ar: AgingBucket[], ap: AgingBucket[]) {
  const lookup = (rows: AgingBucket[], code: string) =>
    rows.find((r) => r.bucket === code)?.outstanding ?? 0;
  return BUCKETS.map((b) => ({
    bucket: b.label,
    AR: lookup(ar, b.code),
    AP: lookup(ap, b.code),
  }));
}

/** Finance dashboard: AR/AP position, aging, cash flow — one query, fixed grid. */
export function FinanceDashboard() {
  const { data, isPending, isError, refetch } = useQuery({
    queryKey: queryKeys.finance.dashboard,
    queryFn: fetchFinanceDashboard,
    refetchInterval: 60_000,
  });

  if (isPending) return <DashboardSkeleton />;
  if (isError) return <ErrorState message="The Finance dashboard didn't load." onRetry={refetch} />;

  const aging = agingSeries(data.arAging, data.apAging);
  const hasAging = data.arAging.length > 0 || data.apAging.length > 0;

  return (
    <div className="space-y-6">
      <PageHeader module="Finance" asOf={new Date(data.asOf)} />

      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard label="AR outstanding" value={money(data.arOutstanding)} />
        <StatCard label="AP outstanding" value={money(data.apOutstanding)} />
        <StatCard
          label={`Overdue AR (${data.overdueArCount})`}
          value={money(data.overdueArAmount)}
        />
        <StatCard
          label="Net position"
          value={money(data.netPosition)}
          delta={{
            value: money(Math.abs(data.netPosition)),
            direction: data.netPosition >= 0 ? "up" : "down",
          }}
        />
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        <DashboardCard title="Aging — AR vs AP" className="lg:col-span-2">
          {!hasAging ? (
            <EmptyState
              title="Nothing outstanding"
              action="Send an invoice or approve a bill to see aging here."
            />
          ) : (
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={aging}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="bucket" />
                <YAxis tickFormatter={(v) => money(v)} width={90} />
                <Tooltip formatter={(v: number) => money(v)} />
                <Legend />
                <Bar dataKey="AR" fill={moduleAccent.finance} radius={[3, 3, 0, 0]} />
                <Bar dataKey="AP" fill={semantic.warning} radius={[3, 3, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </DashboardCard>

        <DashboardCard title="Cash flow — last 30 days">
          <dl className="space-y-4">
            <div>
              <dt className="text-sm text-neutral-500">Cash in (payments received)</dt>
              <dd className="text-xl font-semibold tabular-nums text-positive">
                {money(data.cashInLast30Days)}
              </dd>
            </div>
            <div>
              <dt className="text-sm text-neutral-500">Cash out (payments made)</dt>
              <dd className="text-xl font-semibold tabular-nums text-negative">
                {money(data.cashOutLast30Days)}
              </dd>
            </div>
            <div className="border-t border-neutral-200 pt-3">
              <dt className="text-sm text-neutral-500">Net cash flow</dt>
              <dd className="text-xl font-semibold tabular-nums text-neutral-900">
                {money(data.cashInLast30Days - data.cashOutLast30Days)}
              </dd>
            </div>
          </dl>
        </DashboardCard>
      </div>

      <DashboardCard title="Needs attention — overdue AR and AP due this week">
        {data.needsAttention.length === 0 ? (
          <EmptyState
            title="Nothing needs attention"
            action="Overdue invoices and bills coming due will surface here."
          />
        ) : (
          <DataTable
            rows={data.needsAttention}
            rowKey={(r) => `${r.kind}-${r.documentId}`}
            columns={[
              { header: "Type", cell: (r) => <StatusBadge status={r.kind === "AR" ? "OVERDUE" : "SENT"} /> },
              { header: "Party", cell: (r) => r.party },
              { header: "Due", align: "right", cell: (r) => shortDate(r.dueDate) },
              { header: "Outstanding", align: "right", cell: (r) => money(r.outstanding) },
            ]}
          />
        )}
      </DashboardCard>
    </div>
  );
}
