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
import { createEmployee, fetchDepartments, fetchEmployees, terminateEmployee } from "./api";
import type { EmployeeRequest } from "./types";

const todayIso = () => new Date().toISOString().slice(0, 10);

/** Employees: list, hire, and terminate (records are kept, never deleted). */
export function EmployeesPage() {
  const qc = useQueryClient();
  const [showHire, setShowHire] = useState(false);

  const { data, isPending, isError, refetch } = useQuery({
    queryKey: queryKeys.hr.employees,
    queryFn: fetchEmployees,
  });

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: queryKeys.hr.employees });
    qc.invalidateQueries({ queryKey: queryKeys.hr.dashboard });
  };
  const terminate = useMutation({
    mutationFn: (id: number) => terminateEmployee(id, todayIso()),
    onSuccess: invalidate,
  });

  if (isPending) return <DashboardSkeleton />;
  if (isError) return <ErrorState message="Employees didn't load." onRetry={refetch} />;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <PageHeader module="Employees" asOf={new Date()} />
        <button onClick={() => setShowHire(true)} className="rounded bg-hr px-3 py-2 text-sm font-medium text-white">
          Hire employee
        </button>
      </div>

      <DashboardCard title="All employees">
        {data.length === 0 ? (
          <EmptyState title="No employees yet" action="Hire your first employee to build out the roster." />
        ) : (
          <DataTable
            rows={data}
            rowKey={(r) => r.id}
            columns={[
              { header: "Name", cell: (r) => r.name },
              { header: "Department", cell: (r) => r.departmentName ?? "—" },
              { header: "Position", cell: (r) => r.position ?? "—" },
              { header: "Hired", align: "right", cell: (r) => shortDate(r.hireDate) },
              { header: "Status", cell: (r) => <StatusBadge status={r.status} /> },
              {
                header: "",
                align: "right",
                cell: (r) =>
                  r.status === "TERMINATED" ? null : (
                    <button
                      className="text-sm font-medium text-negative hover:underline"
                      onClick={() => terminate.mutate(r.id)}
                    >
                      Terminate
                    </button>
                  ),
              },
            ]}
          />
        )}
      </DashboardCard>

      {showHire && <HireModal onClose={() => setShowHire(false)} onHired={invalidate} />}
    </div>
  );
}

function HireModal({ onClose, onHired }: { onClose: () => void; onHired: () => void }) {
  const { data: departments } = useQuery({ queryKey: queryKeys.hr.departments, queryFn: fetchDepartments });
  const [form, setForm] = useState<Omit<EmployeeRequest, "departmentId" | "currency"> & { departmentId: number | "" }>({
    name: "",
    email: "",
    departmentId: "",
    position: "",
    hireDate: todayIso(),
    salary: 90000,
  });

  const hire = useMutation({
    mutationFn: (body: EmployeeRequest) => createEmployee(body),
    onSuccess: () => {
      onHired();
      onClose();
    },
  });
  const error = hire.error instanceof ApiError ? hire.error.message : null;

  return (
    <Modal title="Hire employee" onClose={onClose}>
      <form
        className="space-y-4"
        onSubmit={(e) => {
          e.preventDefault();
          if (form.departmentId === "") return;
          hire.mutate({ ...form, departmentId: form.departmentId, currency: "USD" });
        }}
      >
        <label className="block text-sm">
          <span className="text-neutral-600">Name</span>
          <input
            required
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
          />
        </label>
        <div className="grid grid-cols-2 gap-3">
          <label className="block text-sm">
            <span className="text-neutral-600">Email</span>
            <input
              type="email"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
            />
          </label>
          <label className="block text-sm">
            <span className="text-neutral-600">Department</span>
            <select
              required
              value={form.departmentId}
              onChange={(e) => setForm({ ...form, departmentId: e.target.value === "" ? "" : Number(e.target.value) })}
              className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
            >
              <option value="" disabled>
                Select…
              </option>
              {departments?.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.name}
                </option>
              ))}
            </select>
          </label>
          <label className="block text-sm">
            <span className="text-neutral-600">Position</span>
            <input
              value={form.position}
              onChange={(e) => setForm({ ...form, position: e.target.value })}
              className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
            />
          </label>
          <label className="block text-sm">
            <span className="text-neutral-600">Hire date</span>
            <input
              type="date"
              value={form.hireDate}
              onChange={(e) => setForm({ ...form, hireDate: e.target.value })}
              className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
            />
          </label>
          <label className="block text-sm">
            <span className="text-neutral-600">Salary (USD)</span>
            <input
              type="number"
              min="0"
              step="1000"
              value={form.salary}
              onChange={(e) => setForm({ ...form, salary: Number(e.target.value) })}
              className="mt-1 w-full rounded border border-neutral-300 px-2 py-1 text-right tabular-nums"
            />
          </label>
        </div>
        {error && <p className="text-sm text-negative">{error}</p>}
        <button
          type="submit"
          disabled={hire.isPending || form.name.trim() === "" || form.departmentId === ""}
          className="w-full rounded bg-hr px-3 py-2 text-sm font-medium text-white disabled:opacity-50"
        >
          {hire.isPending ? "Hiring…" : "Hire employee"}
        </button>
      </form>
    </Modal>
  );
}
