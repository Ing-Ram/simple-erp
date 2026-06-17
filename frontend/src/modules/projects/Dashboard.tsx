import { useQuery } from "@tanstack/react-query";
import { Bar, BarChart, Cell, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { queryKeys } from "../../lib/queryKeys";
import { count, money, percent, shortDate } from "../../lib/format";
import { semantic } from "../../lib/tokens";
import { PageHeader } from "../../components/PageHeader";
import { StatCard } from "../../components/StatCard";
import { DashboardCard } from "../../components/DashboardCard";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { ErrorState } from "../../components/ErrorState";
import { EmptyState } from "../../components/EmptyState";
import { DashboardSkeleton } from "../../components/DashboardSkeleton";
import { fetchProjectsDashboard } from "./api";
import type { BudgetHealth } from "./types";

/** Bar color by budget health. */
const healthColor: Record<BudgetHealth, string> = {
  ON_TRACK: semantic.positive,
  AT_RISK: semantic.warning,
  OVER: semantic.negative,
};

/** Projects dashboard: budget vs actual, utilization, milestones, needs-attention. */
export function ProjectsDashboard() {
  const { data, isPending, isError, refetch } = useQuery({
    queryKey: queryKeys.projects.dashboard,
    queryFn: fetchProjectsDashboard,
    refetchInterval: 60_000,
  });

  if (isPending) return <DashboardSkeleton />;
  if (isError) return <ErrorState message="The Projects dashboard didn't load." onRetry={refetch} />;

  return (
    <div className="space-y-6">
      <PageHeader module="Projects" asOf={new Date()} />

      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard label="Active projects" value={count(data.activeProjects)} />
        <StatCard label="At-risk / over budget" value={count(data.atRiskOrOverBudget)} />
        <StatCard label="Hours logged this week" value={count(data.hoursThisWeek)} />
        <StatCard label="Utilization, last 30 days" value={percent(data.utilization)} />
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        <DashboardCard title="Budget vs. actual — active projects" className="lg:col-span-2">
          {data.budgetVsActual.length === 0 ? (
            <EmptyState title="No active projects" action="Activate a project to track budget vs. actual." />
          ) : (
            <ResponsiveContainer width="100%" height={Math.max(160, data.budgetVsActual.length * 64)}>
              <BarChart data={data.budgetVsActual} layout="vertical" barGap={2}>
                <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                <XAxis type="number" tickFormatter={(v) => money(v)} />
                <YAxis type="category" dataKey="name" width={130} />
                <Tooltip
                  formatter={(v: number, key) => [money(v), key === "spent" ? "Spent" : "Budget"]}
                />
                <Bar dataKey="budget" fill="#d4d4d4" radius={[0, 3, 3, 0]} />
                <Bar dataKey="spent" radius={[0, 3, 3, 0]}>
                  {data.budgetVsActual.map((r) => (
                    <Cell key={r.projectId} fill={healthColor[r.health]} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          )}
        </DashboardCard>

        <DashboardCard title="Upcoming milestones — next 30 days">
          {data.upcomingMilestones.length === 0 ? (
            <EmptyState title="No milestones due" action="Unresolved milestones due soon appear here." />
          ) : (
            <ul className="divide-y divide-neutral-200">
              {data.upcomingMilestones.map((m) => (
                <li key={m.id} className="flex items-center justify-between py-2">
                  <div>
                    <div className="text-sm text-neutral-800">{m.name}</div>
                    <div className="text-xs text-neutral-500">{m.projectName}</div>
                  </div>
                  <span className={`tabular-nums text-sm ${m.overdue ? "font-medium text-negative" : "text-neutral-500"}`}>
                    {shortDate(m.dueDate)}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </DashboardCard>
      </div>

      <DashboardCard title="Needs attention — overdue tasks and projects past due">
        {data.needsAttention.length === 0 ? (
          <EmptyState
            title="Nothing needs attention"
            action="Overdue tasks and projects past their target end date surface here."
          />
        ) : (
          <DataTable
            rows={data.needsAttention}
            rowKey={(r) => `${r.kind}-${r.id}`}
            columns={[
              { header: "Type", cell: (r) => <StatusBadge status={r.kind === "TASK" ? "AT_RISK" : "OVER"} /> },
              { header: "Item", cell: (r) => r.label },
              { header: "Project", cell: (r) => r.projectName },
              { header: "Due", align: "right", cell: (r) => (r.dueDate ? shortDate(r.dueDate) : "—") },
            ]}
          />
        )}
      </DashboardCard>
    </div>
  );
}
