import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "../../lib/queryKeys";
import { shortDate } from "../../lib/format";
import { ApiError } from "../../lib/api";
import { PageHeader } from "../../components/PageHeader";
import { DashboardCard } from "../../components/DashboardCard";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { ErrorState } from "../../components/ErrorState";
import { EmptyState } from "../../components/EmptyState";
import { DashboardSkeleton } from "../../components/DashboardSkeleton";
import { Modal } from "../../components/Modal";
import {
  approveLeave,
  cancelLeave,
  fetchEmployees,
  fetchLeaveRequests,
  rejectLeave,
  submitLeave,
} from "./api";
import type { LeaveRequest, LeaveRequestRequest, LeaveType } from "./types";

const TYPES: LeaveType[] = ["VACATION", "SICK", "UNPAID", "PARENTAL"];
const todayIso = () => new Date().toISOString().slice(0, 10);
const plusDaysIso = (d: number) => new Date(Date.now() + d * 86_400_000).toISOString().slice(0, 10);

/** Leave requests: submit, and approve / reject / cancel through the workflow. */
export function LeaveRequestsPage() {
  const qc = useQueryClient();
  const [showRequest, setShowRequest] = useState(false);

  const { data, isPending, isError, refetch } = useQuery({
    queryKey: queryKeys.hr.leaveRequests,
    queryFn: fetchLeaveRequests,
  });

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: queryKeys.hr.leaveRequests });
    qc.invalidateQueries({ queryKey: queryKeys.hr.dashboard });
  };
  const approve = useMutation({ mutationFn: approveLeave, onSuccess: invalidate });
  const reject = useMutation({ mutationFn: rejectLeave, onSuccess: invalidate });
  const cancel = useMutation({ mutationFn: cancelLeave, onSuccess: invalidate });

  if (isPending) return <DashboardSkeleton />;
  if (isError) return <ErrorState message="Leave requests didn't load." onRetry={refetch} />;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <PageHeader module="Leave requests" asOf={new Date()} />
        <button onClick={() => setShowRequest(true)} className="rounded bg-hr px-3 py-2 text-sm font-medium text-white">
          Request leave
        </button>
      </div>

      <DashboardCard title="All leave requests">
        {data.length === 0 ? (
          <EmptyState title="No leave requests" action="Submit time off with “Request leave.”" />
        ) : (
          <DataTable
            rows={data}
            rowKey={(r) => r.id}
            columns={[
              { header: "Employee", cell: (r) => r.employeeName },
              { header: "Type", cell: (r) => <StatusBadge status={r.type} /> },
              {
                header: "Dates",
                align: "right",
                cell: (r) => (
                  <span className="tabular-nums">
                    {shortDate(r.startDate)} – {shortDate(r.endDate)}
                  </span>
                ),
              },
              { header: "Days", align: "right", cell: (r) => r.businessDays },
              { header: "Status", cell: (r) => <StatusBadge status={r.status} /> },
              { header: "", align: "right", cell: (r) => <RowActions leave={r} /> },
            ]}
          />
        )}
      </DashboardCard>

      {showRequest && <RequestLeaveModal onClose={() => setShowRequest(false)} onSubmitted={invalidate} />}
    </div>
  );

  function RowActions({ leave }: { leave: LeaveRequest }) {
    return (
      <div className="flex justify-end gap-3 text-sm font-medium">
        {leave.status === "PENDING" && (
          <>
            <button className="text-positive hover:underline" onClick={() => approve.mutate(leave.id)}>
              Approve
            </button>
            <button className="text-negative hover:underline" onClick={() => reject.mutate(leave.id)}>
              Reject
            </button>
          </>
        )}
        {leave.status === "APPROVED" && (
          <button className="text-neutral-600 hover:underline" onClick={() => cancel.mutate(leave.id)}>
            Cancel
          </button>
        )}
      </div>
    );
  }
}

function RequestLeaveModal({ onClose, onSubmitted }: { onClose: () => void; onSubmitted: () => void }) {
  const { data: employees } = useQuery({ queryKey: queryKeys.hr.employees, queryFn: fetchEmployees });
  const [employeeId, setEmployeeId] = useState<number | "">("");
  const [type, setType] = useState<LeaveType>("VACATION");
  const [startDate, setStartDate] = useState(plusDaysIso(7));
  const [endDate, setEndDate] = useState(plusDaysIso(9));

  const submit = useMutation({
    mutationFn: (body: LeaveRequestRequest) => submitLeave(body),
    onSuccess: () => {
      onSubmitted();
      onClose();
    },
  });
  const error = submit.error instanceof ApiError ? submit.error.message : null;

  return (
    <Modal title="Request leave" onClose={onClose}>
      <form
        className="space-y-4"
        onSubmit={(e) => {
          e.preventDefault();
          if (employeeId === "") return;
          submit.mutate({ employeeId, type, startDate, endDate });
        }}
      >
        <label className="block text-sm">
          <span className="text-neutral-600">Employee</span>
          <select
            required
            value={employeeId}
            onChange={(e) => setEmployeeId(e.target.value === "" ? "" : Number(e.target.value))}
            className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
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
        <label className="block text-sm">
          <span className="text-neutral-600">Type</span>
          <select
            value={type}
            onChange={(e) => setType(e.target.value as LeaveType)}
            className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
          >
            {TYPES.map((t) => (
              <option key={t} value={t}>
                {t.toLowerCase()}
              </option>
            ))}
          </select>
        </label>
        <div className="grid grid-cols-2 gap-3">
          <label className="block text-sm">
            <span className="text-neutral-600">Start</span>
            <input
              type="date"
              min={todayIso()}
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
            />
          </label>
          <label className="block text-sm">
            <span className="text-neutral-600">End</span>
            <input
              type="date"
              min={startDate}
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
            />
          </label>
        </div>
        {error && <p className="text-sm text-negative">{error}</p>}
        <button
          type="submit"
          disabled={submit.isPending || employeeId === ""}
          className="w-full rounded bg-hr px-3 py-2 text-sm font-medium text-white disabled:opacity-50"
        >
          {submit.isPending ? "Submitting…" : "Submit request"}
        </button>
      </form>
    </Modal>
  );
}
