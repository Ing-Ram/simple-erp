import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Bar, BarChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { queryKeys } from "../../lib/queryKeys";
import { count, percent, shortDate } from "../../lib/format";
import { moduleAccent } from "../../lib/tokens";
import { PageHeader } from "../../components/PageHeader";
import { StatCard } from "../../components/StatCard";
import { DashboardCard } from "../../components/DashboardCard";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { ErrorState } from "../../components/ErrorState";
import { EmptyState } from "../../components/EmptyState";
import { DashboardSkeleton } from "../../components/DashboardSkeleton";
import { approveLeave, fetchHrDashboard, rejectLeave } from "./api";

/** HR dashboard: headcount, hiring, and leave — one query rendered into the shared grid. */
export function HrDashboard() {
  const queryClient = useQueryClient();
  const { data, isPending, isError, refetch } = useQuery({
    queryKey: queryKeys.hr.dashboard,
    queryFn: fetchHrDashboard,
  });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: queryKeys.hr.dashboard });
  const approve = useMutation({ mutationFn: approveLeave, onSuccess: invalidate });
  const reject = useMutation({ mutationFn: rejectLeave, onSuccess: invalidate });

  if (isPending) return <DashboardSkeleton />;
  if (isError) return <ErrorState message="The HR dashboard didn't load." onRetry={refetch} />;

  return (
    <div className="space-y-6">
      <PageHeader module="HR" asOf={new Date()} />

      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard label="Active headcount" value={count(data.activeHeadcount)} />
        <StatCard label="Hires, last 90 days" value={count(data.hiresLast90Days)} />
        <StatCard label="Pending leave requests" value={count(data.pendingLeaveRequests)} />
        <StatCard label="Turnover, trailing 12 mo" value={percent(data.turnoverRate)} />
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        <DashboardCard title="Headcount by department" className="lg:col-span-2">
          {data.headcountByDepartment.length === 0 ? (
            <EmptyState
              title="No employees yet"
              action="Add your first employee to see headcount by department."
            />
          ) : (
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={data.headcountByDepartment} layout="vertical">
                <XAxis type="number" allowDecimals={false} />
                <YAxis type="category" dataKey="department" width={120} />
                <Tooltip />
                <Bar dataKey="headcount" fill={moduleAccent.hr} radius={[0, 3, 3, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </DashboardCard>

        <DashboardCard title="Who's out — today and next 14 days">
          {data.whosOut.length === 0 ? (
            <EmptyState title="Nobody is out" action="Approved leave in this window appears here." />
          ) : (
            <ul className="divide-y divide-neutral-200">
              {data.whosOut.map((o) => (
                <li key={`${o.employeeId}-${o.startDate}`} className="flex justify-between py-2">
                  <span>{o.name}</span>
                  <span className="tabular-nums text-neutral-500">
                    {shortDate(o.startDate)} – {shortDate(o.endDate)}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </DashboardCard>
      </div>

      <DashboardCard title="Pending leave requests">
        {data.needsAttention.length === 0 ? (
          <EmptyState title="No pending requests" action="New leave requests will land here for review." />
        ) : (
          <DataTable
            rows={data.needsAttention}
            rowKey={(r) => r.requestId}
            columns={[
              { header: "Employee", cell: (r) => r.name },
              { header: "Type", cell: (r) => <StatusBadge status={r.leaveType} /> },
              {
                header: "Dates",
                align: "right",
                cell: (r) => (
                  <span className="tabular-nums">
                    {shortDate(r.startDate)} – {shortDate(r.endDate)}
                  </span>
                ),
              },
              { header: "Requested", align: "right", cell: (r) => shortDate(r.requestedOn) },
              {
                header: "",
                align: "right",
                cell: (r) => (
                  <div className="flex justify-end gap-3 text-sm font-medium">
                    <button className="text-positive hover:underline" onClick={() => approve.mutate(r.requestId)}>
                      Approve
                    </button>
                    <button className="text-negative hover:underline" onClick={() => reject.mutate(r.requestId)}>
                      Reject
                    </button>
                  </div>
                ),
              },
            ]}
          />
        )}
      </DashboardCard>
    </div>
  );
}
