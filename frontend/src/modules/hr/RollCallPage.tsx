import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "../../lib/queryKeys";
import { clockTime, count } from "../../lib/format";
import { PageHeader } from "../../components/PageHeader";
import { StatCard } from "../../components/StatCard";
import { DashboardCard } from "../../components/DashboardCard";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { ErrorState } from "../../components/ErrorState";
import { DashboardSkeleton } from "../../components/DashboardSkeleton";
import { checkIn, checkOut, fetchEmployees, fetchRollCall } from "./api";
import type { WorkMode } from "./types";

/** Emergency roll-call: account for every active employee, with check-in/out controls. */
export function RollCallPage() {
  const qc = useQueryClient();
  const [employeeId, setEmployeeId] = useState<number | "">("");
  const [workMode, setWorkMode] = useState<WorkMode>("ON_SITE");

  // Poll every 15s so a live muster stays current during an incident without manual refreshes.
  const roll = useQuery({
    queryKey: queryKeys.hr.rollCall,
    queryFn: fetchRollCall,
    refetchInterval: 15_000,
  });
  const { data: employees } = useQuery({ queryKey: queryKeys.hr.employees, queryFn: fetchEmployees });

  const refresh = () => qc.invalidateQueries({ queryKey: queryKeys.hr.rollCall });
  const checkInMut = useMutation({
    mutationFn: () => checkIn(employeeId as number, workMode),
    onSuccess: () => {
      setEmployeeId("");
      refresh();
    },
  });
  const checkOutMut = useMutation({ mutationFn: checkOut, onSuccess: refresh });

  if (roll.isPending) return <DashboardSkeleton />;
  if (roll.isError) return <ErrorState message="The roll-call didn't load." onRetry={roll.refetch} />;
  const data = roll.data;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <PageHeader module="Roll Call" asOf={new Date(data.asOf)} />
        <button
          onClick={refresh}
          disabled={roll.isFetching}
          className="rounded border border-neutral-300 px-3 py-2 text-sm font-medium text-neutral-700 hover:bg-neutral-50 disabled:opacity-50"
        >
          {roll.isFetching ? "Refreshing…" : "Refresh"}
        </button>
      </div>

      {data.unaccountedCount > 0 && (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-negative">
          {count(data.unaccountedCount)} active{" "}
          {data.unaccountedCount === 1 ? "person is" : "people are"} unaccounted for — locate before
          clearing the building.
        </div>
      )}

      <div className="grid grid-cols-2 gap-4 lg:grid-cols-5">
        <StatCard label="Present" value={count(data.presentCount)} />
        <StatCard label="Checked out" value={count(data.checkedOutCount)} />
        <StatCard label="Remote" value={count(data.remoteCount)} />
        <StatCard label="On leave" value={count(data.onLeaveCount)} />
        <StatCard label="Unaccounted" value={count(data.unaccountedCount)} />
      </div>

      <DashboardCard title="Check in">
        <form
          className="flex flex-wrap items-end gap-3"
          onSubmit={(e) => {
            e.preventDefault();
            if (employeeId !== "") checkInMut.mutate();
          }}
        >
          <label className="text-sm">
            <span className="text-neutral-600">Employee</span>
            <select
              required
              value={employeeId}
              onChange={(e) => setEmployeeId(e.target.value === "" ? "" : Number(e.target.value))}
              className="mt-1 block w-56 rounded border border-neutral-300 px-2 py-1"
            >
              <option value="" disabled>
                Select an employee…
              </option>
              {employees
                ?.filter((emp) => emp.status !== "TERMINATED")
                .map((emp) => (
                  <option key={emp.id} value={emp.id}>
                    {emp.name}
                  </option>
                ))}
            </select>
          </label>
          <label className="text-sm">
            <span className="text-neutral-600">Mode</span>
            <select
              value={workMode}
              onChange={(e) => setWorkMode(e.target.value as WorkMode)}
              className="mt-1 block w-40 rounded border border-neutral-300 px-2 py-1"
            >
              <option value="ON_SITE">On site</option>
              <option value="REMOTE">Remote</option>
            </select>
          </label>
          <button
            type="submit"
            disabled={employeeId === "" || checkInMut.isPending}
            className="rounded bg-hr px-3 py-2 text-sm font-medium text-white disabled:opacity-50"
          >
            {checkInMut.isPending ? "Checking in…" : "Check in"}
          </button>
        </form>
      </DashboardCard>

      <DashboardCard title="Roster">
        <DataTable
          rows={data.entries}
          rowKey={(r) => r.employeeId}
          columns={[
            { header: "Name", cell: (r) => r.name },
            { header: "Department", cell: (r) => r.department ?? "—" },
            { header: "Status", cell: (r) => <StatusBadge status={r.status} /> },
            { header: "Since", align: "right", cell: (r) => (r.since ? clockTime(r.since) : "—") },
            {
              header: "",
              align: "right",
              cell: (r) =>
                r.status === "PRESENT" ? (
                  <button
                    className="text-sm font-medium text-neutral-600 hover:underline"
                    onClick={() => checkOutMut.mutate(r.employeeId)}
                  >
                    Check out
                  </button>
                ) : null,
            },
          ]}
        />
      </DashboardCard>
    </div>
  );
}
