import { useQuery } from "@tanstack/react-query";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { queryKeys } from "../../lib/queryKeys";
import { count, money, percent, shortDate } from "../../lib/format";
import { moduleAccent } from "../../lib/tokens";
import { PageHeader } from "../../components/PageHeader";
import { StatCard } from "../../components/StatCard";
import { DashboardCard } from "../../components/DashboardCard";
import { DataTable } from "../../components/DataTable";
import { ErrorState } from "../../components/ErrorState";
import { EmptyState } from "../../components/EmptyState";
import { DashboardSkeleton } from "../../components/DashboardSkeleton";
import { fetchSalesDashboard } from "./api";

/** Title-cases a stage/enum code, e.g. NEGOTIATION → Negotiation. */
const titleCase = (s: string) => s.charAt(0) + s.slice(1).toLowerCase();
const monthLabel = (m: string) => new Date(`${m}-01`).toLocaleDateString("en-US", { month: "short" });

/** Sales dashboard: pipeline, win metrics, and money sitting still — one query, fixed grid. */
export function SalesDashboard() {
  const { data, isPending, isError, refetch } = useQuery({
    queryKey: queryKeys.sales.dashboard,
    queryFn: fetchSalesDashboard,
    refetchInterval: 60_000,
  });

  if (isPending) return <DashboardSkeleton />;
  if (isError) return <ErrorState message="The Sales dashboard didn't load." onRetry={refetch} />;

  const funnel = data.funnel.map((f) => ({ stage: titleCase(f.stage), value: f.value, count: f.count }));
  const monthly = data.monthlyWon.map((m) => ({ label: monthLabel(m.month), amount: m.amount }));
  const hasPipeline = data.funnel.some((f) => f.count > 0);

  return (
    <div className="space-y-6">
      <PageHeader module="Sales" asOf={new Date()} />

      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard label="Open pipeline (weighted)" value={money(data.openPipelineWeighted)} />
        <StatCard label="Won this quarter" value={money(data.wonThisQuarter)} />
        <StatCard label="Win rate, last 90 days" value={percent(data.winRateLast90Days)} />
        <StatCard label="Average deal size" value={money(data.averageDealSize)} />
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        <DashboardCard title="Pipeline by stage" className="lg:col-span-2">
          {!hasPipeline ? (
            <EmptyState title="No open opportunities" action="Qualify a lead to start the pipeline." />
          ) : (
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={funnel} layout="vertical">
                <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                <XAxis type="number" tickFormatter={(v) => money(v)} />
                <YAxis type="category" dataKey="stage" width={100} />
                <Tooltip
                  formatter={(v: number, name) => (name === "value" ? money(v) : count(v))}
                  labelFormatter={(l) => l}
                />
                <Bar dataKey="value" fill={moduleAccent.sales} radius={[0, 3, 3, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </DashboardCard>

        <DashboardCard title="Won revenue — trailing 6 months">
          <ResponsiveContainer width="100%" height={280}>
            <LineChart data={monthly}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="label" />
              <YAxis tickFormatter={(v) => money(v)} width={80} />
              <Tooltip formatter={(v: number) => money(v)} />
              <Line type="monotone" dataKey="amount" stroke={moduleAccent.sales} strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
        </DashboardCard>
      </div>

      <DashboardCard title="Needs attention — stale opportunities and uninvoiced orders">
        {data.needsAttention.length === 0 ? (
          <EmptyState
            title="Nothing needs attention"
            action="Overdue opportunities and fulfilled-but-uninvoiced orders surface here."
          />
        ) : (
          <DataTable
            rows={data.needsAttention}
            rowKey={(r) => `${r.kind}-${r.id}`}
            columns={[
              {
                header: "Item",
                cell: (r) =>
                  r.kind === "OPPORTUNITY" ? `Opportunity #${r.id}` : `Order #${r.id}`,
              },
              {
                header: "Why",
                cell: (r) => (r.kind === "OPPORTUNITY" ? "Past expected close" : "Awaiting invoice"),
              },
              { header: "Customer", cell: (r) => r.customerName },
              { header: "Date", align: "right", cell: (r) => shortDate(r.date) },
              { header: "Value", align: "right", cell: (r) => money(r.amount) },
            ]}
          />
        )}
      </DashboardCard>
    </div>
  );
}
