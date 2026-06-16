import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "../../lib/queryKeys";
import { ApiError } from "../../lib/api";
import { PageHeader } from "../../components/PageHeader";
import { DashboardCard } from "../../components/DashboardCard";
import { DataTable } from "../../components/DataTable";
import { ErrorState } from "../../components/ErrorState";
import { EmptyState } from "../../components/EmptyState";
import { DashboardSkeleton } from "../../components/DashboardSkeleton";
import { Modal } from "../../components/Modal";
import { createDepartment, fetchDepartments, fetchEmployees } from "./api";
import type { DepartmentRequest } from "./types";

/** Departments: list and create (with an optional manager). */
export function DepartmentsPage() {
  const qc = useQueryClient();
  const [showCreate, setShowCreate] = useState(false);

  const { data, isPending, isError, refetch } = useQuery({
    queryKey: queryKeys.hr.departments,
    queryFn: fetchDepartments,
  });

  if (isPending) return <DashboardSkeleton />;
  if (isError) return <ErrorState message="Departments didn't load." onRetry={refetch} />;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <PageHeader module="Departments" asOf={new Date()} />
        <button onClick={() => setShowCreate(true)} className="rounded bg-hr px-3 py-2 text-sm font-medium text-white">
          New department
        </button>
      </div>

      <DashboardCard title="All departments">
        {data.length === 0 ? (
          <EmptyState title="No departments yet" action="Create a department to organize employees." />
        ) : (
          <DataTable
            rows={data}
            rowKey={(r) => r.id}
            columns={[
              { header: "Name", cell: (r) => r.name },
              { header: "Manager", cell: (r) => r.managerName ?? "—" },
            ]}
          />
        )}
      </DashboardCard>

      {showCreate && (
        <CreateDepartmentModal
          onClose={() => setShowCreate(false)}
          onCreated={() => qc.invalidateQueries({ queryKey: queryKeys.hr.departments })}
        />
      )}
    </div>
  );
}

function CreateDepartmentModal({ onClose, onCreated }: { onClose: () => void; onCreated: () => void }) {
  const { data: employees } = useQuery({ queryKey: queryKeys.hr.employees, queryFn: fetchEmployees });
  const [name, setName] = useState("");
  const [managerId, setManagerId] = useState<number | "">("");

  const create = useMutation({
    mutationFn: (body: DepartmentRequest) => createDepartment(body),
    onSuccess: () => {
      onCreated();
      onClose();
    },
  });
  const error = create.error instanceof ApiError ? create.error.message : null;

  return (
    <Modal title="New department" onClose={onClose}>
      <form
        className="space-y-4"
        onSubmit={(e) => {
          e.preventDefault();
          create.mutate({ name, managerId: managerId === "" ? null : managerId });
        }}
      >
        <label className="block text-sm">
          <span className="text-neutral-600">Name</span>
          <input
            required
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
          />
        </label>
        <label className="block text-sm">
          <span className="text-neutral-600">Manager (optional)</span>
          <select
            value={managerId}
            onChange={(e) => setManagerId(e.target.value === "" ? "" : Number(e.target.value))}
            className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
          >
            <option value="">— none —</option>
            {employees
              ?.filter((emp) => emp.status !== "TERMINATED")
              .map((emp) => (
                <option key={emp.id} value={emp.id}>
                  {emp.name}
                </option>
              ))}
          </select>
        </label>
        {error && <p className="text-sm text-negative">{error}</p>}
        <button
          type="submit"
          disabled={create.isPending || name.trim() === ""}
          className="w-full rounded bg-hr px-3 py-2 text-sm font-medium text-white disabled:opacity-50"
        >
          {create.isPending ? "Saving…" : "Create department"}
        </button>
      </form>
    </Modal>
  );
}
