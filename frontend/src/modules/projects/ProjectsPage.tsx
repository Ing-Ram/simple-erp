import { useState } from "react";
import { Link } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "../../lib/queryKeys";
import { money, percent } from "../../lib/format";
import { ApiError } from "../../lib/api";
import { PageHeader } from "../../components/PageHeader";
import { DashboardCard } from "../../components/DashboardCard";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { ErrorState } from "../../components/ErrorState";
import { EmptyState } from "../../components/EmptyState";
import { DashboardSkeleton } from "../../components/DashboardSkeleton";
import { Modal } from "../../components/Modal";
import { fetchCustomers } from "../finance/api";
import { fetchEmployees } from "../hr/api";
import {
  activateProject,
  cancelProject,
  completeProject,
  createProject,
  fetchProjects,
  holdProject,
} from "./api";
import type { Project, ProjectRequest } from "./types";

const todayIso = () => new Date().toISOString().slice(0, 10);
const plusDaysIso = (d: number) => new Date(Date.now() + d * 86_400_000).toISOString().slice(0, 10);

/** Projects: list with budget vs actual, create, and drive the lifecycle. */
export function ProjectsPage() {
  const qc = useQueryClient();
  const [showCreate, setShowCreate] = useState(false);

  const { data, isPending, isError, refetch } = useQuery({
    queryKey: queryKeys.projects.list,
    queryFn: fetchProjects,
  });

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: queryKeys.projects.list });
    qc.invalidateQueries({ queryKey: queryKeys.projects.dashboard });
  };
  const activate = useMutation({ mutationFn: activateProject, onSuccess: invalidate });
  const hold = useMutation({ mutationFn: holdProject, onSuccess: invalidate });
  const complete = useMutation({ mutationFn: completeProject, onSuccess: invalidate });
  const cancel = useMutation({ mutationFn: cancelProject, onSuccess: invalidate });

  if (isPending) return <DashboardSkeleton />;
  if (isError) return <ErrorState message="Projects didn't load." onRetry={refetch} />;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <PageHeader module="Projects" asOf={new Date()} />
        <button onClick={() => setShowCreate(true)} className="rounded bg-projects px-3 py-2 text-sm font-medium text-white">
          New project
        </button>
      </div>

      <DashboardCard title="All projects">
        {data.length === 0 ? (
          <EmptyState title="No projects yet" action="Create a project to plan work and track budget." />
        ) : (
          <DataTable
            rows={data}
            rowKey={(r) => r.id}
            columns={[
              {
                header: "Name",
                cell: (r) => (
                  <Link to={`/projects/all/${r.id}`} className="font-medium text-projects hover:underline">
                    {r.name}
                  </Link>
                ),
              },
              { header: "Customer", cell: (r) => r.customerName ?? "Internal" },
              { header: "Manager", cell: (r) => r.managerName },
              { header: "Budget", align: "right", cell: (r) => money(r.budget, r.currency) },
              { header: "Spent", align: "right", cell: (r) => money(r.spent, r.currency) },
              {
                header: "Health",
                cell: (r) => (
                  <span className="tabular-nums">
                    <StatusBadge status={r.budgetHealth} /> {percent(r.percentConsumed)}
                  </span>
                ),
              },
              { header: "Status", cell: (r) => <StatusBadge status={r.status} /> },
              { header: "", align: "right", cell: (r) => <RowActions project={r} /> },
            ]}
          />
        )}
      </DashboardCard>

      {showCreate && <CreateProjectModal onClose={() => setShowCreate(false)} onCreated={invalidate} />}
    </div>
  );

  function RowActions({ project }: { project: Project }) {
    const actions = [];
    if (project.status === "PLANNED" || project.status === "ON_HOLD") {
      actions.push(
        <button key="a" className="text-projects hover:underline" onClick={() => activate.mutate(project.id)}>
          Activate
        </button>,
      );
    }
    if (project.status === "ACTIVE") {
      actions.push(
        <button key="h" className="text-projects hover:underline" onClick={() => hold.mutate(project.id)}>
          Hold
        </button>,
      );
    }
    if (project.status === "ACTIVE" || project.status === "ON_HOLD") {
      actions.push(
        <button key="c" className="text-positive hover:underline" onClick={() => complete.mutate(project.id)}>
          Complete
        </button>,
      );
    }
    if (project.status !== "COMPLETED" && project.status !== "CANCELLED") {
      actions.push(
        <button key="x" className="text-negative hover:underline" onClick={() => cancel.mutate(project.id)}>
          Cancel
        </button>,
      );
    }
    return <div className="flex justify-end gap-3 text-sm font-medium">{actions}</div>;
  }
}

/** New-project form. */
function CreateProjectModal({ onClose, onCreated }: { onClose: () => void; onCreated: () => void }) {
  const { data: customers } = useQuery({ queryKey: queryKeys.finance.customers, queryFn: fetchCustomers });
  const { data: employees } = useQuery({ queryKey: queryKeys.hr.employees, queryFn: fetchEmployees });
  const [name, setName] = useState("");
  const [customerId, setCustomerId] = useState<number | "">("");
  const [managerEmployeeId, setManagerEmployeeId] = useState<number | "">("");
  const [startDate, setStartDate] = useState(todayIso());
  const [targetEndDate, setTargetEndDate] = useState(plusDaysIso(90));
  const [budget, setBudget] = useState(10000);

  const create = useMutation({
    mutationFn: (body: ProjectRequest) => createProject(body),
    onSuccess: () => {
      onCreated();
      onClose();
    },
  });
  const error = create.error instanceof ApiError ? create.error.message : null;

  return (
    <Modal title="New project" onClose={onClose}>
      <form
        className="space-y-4"
        onSubmit={(e) => {
          e.preventDefault();
          if (managerEmployeeId === "") return;
          create.mutate({
            name,
            customerId: customerId === "" ? null : customerId,
            managerEmployeeId,
            startDate,
            targetEndDate,
            budget,
            currency: "USD",
          });
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
        <div className="grid grid-cols-2 gap-3">
          <label className="block text-sm">
            <span className="text-neutral-600">Customer (optional)</span>
            <select
              value={customerId}
              onChange={(e) => setCustomerId(e.target.value === "" ? "" : Number(e.target.value))}
              className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
            >
              <option value="">— internal —</option>
              {customers?.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </label>
          <label className="block text-sm">
            <span className="text-neutral-600">Manager</span>
            <select
              required
              value={managerEmployeeId}
              onChange={(e) => setManagerEmployeeId(e.target.value === "" ? "" : Number(e.target.value))}
              className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
            >
              <option value="" disabled>
                Select…
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
            <span className="text-neutral-600">Start</span>
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
            />
          </label>
          <label className="block text-sm">
            <span className="text-neutral-600">Target end</span>
            <input
              type="date"
              value={targetEndDate}
              onChange={(e) => setTargetEndDate(e.target.value)}
              className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
            />
          </label>
          <label className="block text-sm">
            <span className="text-neutral-600">Budget (USD)</span>
            <input
              type="number"
              min="0"
              step="500"
              value={budget}
              onChange={(e) => setBudget(Number(e.target.value))}
              className="mt-1 w-full rounded border border-neutral-300 px-2 py-1 text-right tabular-nums"
            />
          </label>
        </div>
        {error && <p className="text-sm text-negative">{error}</p>}
        <button
          type="submit"
          disabled={create.isPending || name.trim() === "" || managerEmployeeId === ""}
          className="w-full rounded bg-projects px-3 py-2 text-sm font-medium text-white disabled:opacity-50"
        >
          {create.isPending ? "Creating…" : "Create project"}
        </button>
      </form>
    </Modal>
  );
}
