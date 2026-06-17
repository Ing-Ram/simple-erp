import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "../../lib/queryKeys";
import { money, percent, shortDate } from "../../lib/format";
import { ApiError } from "../../lib/api";
import { DashboardCard } from "../../components/DashboardCard";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { ErrorState } from "../../components/ErrorState";
import { EmptyState } from "../../components/EmptyState";
import { DashboardSkeleton } from "../../components/DashboardSkeleton";
import { Modal } from "../../components/Modal";
import { fetchEmployees } from "../hr/api";
import {
  assignTask,
  changeTaskStatus,
  completeMilestone,
  createMilestone,
  createTask,
  fetchMilestones,
  fetchProject,
  fetchTasks,
  fetchTimeEntries,
  logTime,
  waiveMilestone,
} from "./api";
import type { Task, TaskStatus, TimeEntryRequest } from "./types";

const TASK_STATUSES: TaskStatus[] = ["TODO", "IN_PROGRESS", "DONE"];
const todayIso = () => new Date().toISOString().slice(0, 10);

/** Read + manage one project: summary, tasks, milestones, and time logging. */
export function ProjectDetailPage() {
  const id = Number(useParams().id);
  const qc = useQueryClient();
  const [showNewTask, setShowNewTask] = useState(false);
  const [showNewMilestone, setShowNewMilestone] = useState(false);
  const [logTask, setLogTask] = useState<Task | null>(null);
  const [viewTimeTask, setViewTimeTask] = useState<Task | null>(null);

  const project = useQuery({ queryKey: queryKeys.projects.project(id), queryFn: () => fetchProject(id) });
  const tasks = useQuery({ queryKey: queryKeys.projects.tasks(id), queryFn: () => fetchTasks(id) });
  const milestones = useQuery({
    queryKey: queryKeys.projects.milestones(id),
    queryFn: () => fetchMilestones(id),
  });
  const { data: employees } = useQuery({ queryKey: queryKeys.hr.employees, queryFn: fetchEmployees });

  /** Tasks/time/milestones all feed the project's budget and the dashboard, so refresh broadly. */
  const invalidate = () => {
    qc.invalidateQueries({ queryKey: queryKeys.projects.project(id) });
    qc.invalidateQueries({ queryKey: queryKeys.projects.tasks(id) });
    qc.invalidateQueries({ queryKey: queryKeys.projects.milestones(id) });
    qc.invalidateQueries({ queryKey: queryKeys.projects.list });
    qc.invalidateQueries({ queryKey: queryKeys.projects.dashboard });
    qc.invalidateQueries({ queryKey: ["projects", "time-entries"] });
  };

  const taskStatus = useMutation({
    mutationFn: (v: { id: number; status: TaskStatus }) => changeTaskStatus(v.id, v.status),
    onSuccess: invalidate,
  });
  const assign = useMutation({
    mutationFn: (v: { id: number; employeeId: number | null }) => assignTask(v.id, v.employeeId),
    onSuccess: invalidate,
  });
  const completeM = useMutation({ mutationFn: completeMilestone, onSuccess: invalidate });
  const waiveM = useMutation({ mutationFn: waiveMilestone, onSuccess: invalidate });

  if (project.isPending) return <DashboardSkeleton />;
  if (project.isError) return <ErrorState message="That project didn't load." onRetry={project.refetch} />;
  const p = project.data;

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <Link to="/projects/all" className="text-sm text-neutral-500 hover:text-neutral-900">
          ← Projects
        </Link>
        <h1 className="text-xl font-semibold tracking-tight">{p.name}</h1>
        <StatusBadge status={p.status} />
        <StatusBadge status={p.budgetHealth} />
      </div>

      <DashboardCard title={p.customerName ?? "Internal project"}>
        <dl className="grid grid-cols-2 gap-x-8 gap-y-3 text-sm sm:grid-cols-3">
          <Field label="Manager" value={p.managerName} />
          <Field label="Dates" value={`${p.startDate ? shortDate(p.startDate) : "—"} → ${p.targetEndDate ? shortDate(p.targetEndDate) : "—"}`} />
          <Field label="Budget" value={money(p.budget, p.currency)} />
          <Field label="Spent" value={money(p.spent, p.currency)} />
          <Field label="Consumed" value={percent(p.percentConsumed)} emphasize />
        </dl>
      </DashboardCard>

      <DashboardCard title="Tasks">
        <div className="mb-3 flex justify-end">
          <button onClick={() => setShowNewTask(true)} className="text-sm font-medium text-projects hover:underline">
            + Add task
          </button>
        </div>
        {tasks.data && tasks.data.length > 0 ? (
          <DataTable
            rows={tasks.data}
            rowKey={(t) => t.id}
            columns={[
              { header: "Title", cell: (t) => t.title },
              {
                header: "Assignee",
                cell: (t) => (
                  <select
                    value={t.assigneeEmployeeId ?? ""}
                    onChange={(e) =>
                      assign.mutate({ id: t.id, employeeId: e.target.value === "" ? null : Number(e.target.value) })
                    }
                    className="rounded border border-neutral-300 px-1 py-0.5 text-xs"
                  >
                    <option value="">— unassigned —</option>
                    {employees
                      ?.filter((emp) => emp.status !== "TERMINATED")
                      .map((emp) => (
                        <option key={emp.id} value={emp.id}>
                          {emp.name}
                        </option>
                      ))}
                  </select>
                ),
              },
              {
                header: "Status",
                cell: (t) => (
                  <select
                    value={t.status}
                    onChange={(e) => taskStatus.mutate({ id: t.id, status: e.target.value as TaskStatus })}
                    className="rounded border border-neutral-300 px-1 py-0.5 text-xs"
                  >
                    {TASK_STATUSES.map((s) => (
                      <option key={s} value={s}>
                        {s.replace(/_/g, " ").toLowerCase()}
                      </option>
                    ))}
                  </select>
                ),
              },
              { header: "Due", align: "right", cell: (t) => (t.dueDate ? shortDate(t.dueDate) : "—") },
              {
                header: "",
                align: "right",
                cell: (t) => (
                  <div className="flex justify-end gap-3 text-sm font-medium">
                    <button
                      className="text-neutral-600 hover:underline"
                      onClick={() => setViewTimeTask((cur) => (cur?.id === t.id ? null : t))}
                    >
                      {viewTimeTask?.id === t.id ? "Hide time" : "View time"}
                    </button>
                    <button
                      className="text-projects hover:underline disabled:text-neutral-300"
                      disabled={p.status === "COMPLETED" || p.status === "CANCELLED"}
                      onClick={() => setLogTask(t)}
                    >
                      Log time
                    </button>
                  </div>
                ),
              },
            ]}
          />
        ) : (
          <EmptyState title="No tasks yet" action="Add a task to start logging work." />
        )}
      </DashboardCard>

      {viewTimeTask && <TimeEntriesCard task={viewTimeTask} />}

      <DashboardCard title="Milestones">
        <div className="mb-3 flex justify-end">
          <button onClick={() => setShowNewMilestone(true)} className="text-sm font-medium text-projects hover:underline">
            + Add milestone
          </button>
        </div>
        {milestones.data && milestones.data.length > 0 ? (
          <DataTable
            rows={milestones.data}
            rowKey={(m) => m.id}
            columns={[
              { header: "Name", cell: (m) => m.name },
              { header: "Due", align: "right", cell: (m) => (m.dueDate ? shortDate(m.dueDate) : "—") },
              {
                header: "State",
                cell: (m) => (
                  <StatusBadge status={m.completedAt ? "COMPLETED" : m.waived ? "ON_HOLD" : "PENDING"} />
                ),
              },
              {
                header: "",
                align: "right",
                cell: (m) =>
                  m.resolved ? null : (
                    <div className="flex justify-end gap-3 text-sm font-medium">
                      <button className="text-positive hover:underline" onClick={() => completeM.mutate(m.id)}>
                        Complete
                      </button>
                      <button className="text-neutral-600 hover:underline" onClick={() => waiveM.mutate(m.id)}>
                        Waive
                      </button>
                    </div>
                  ),
              },
            ]}
          />
        ) : (
          <EmptyState title="No milestones" action="Add milestones to gate project completion." />
        )}
      </DashboardCard>

      {showNewTask && (
        <NewTaskModal projectId={id} onClose={() => setShowNewTask(false)} onCreated={invalidate} />
      )}
      {showNewMilestone && (
        <NewMilestoneModal projectId={id} onClose={() => setShowNewMilestone(false)} onCreated={invalidate} />
      )}
      {logTask && (
        <LogTimeModal task={logTask} onClose={() => setLogTask(null)} onLogged={invalidate} />
      )}
    </div>
  );
}

function Field({ label, value, emphasize }: { label: string; value: string; emphasize?: boolean }) {
  return (
    <div>
      <dt className="text-neutral-500">{label}</dt>
      <dd className={`tabular-nums ${emphasize ? "font-semibold text-neutral-900" : "text-neutral-800"}`}>
        {value}
      </dd>
    </div>
  );
}

/** Read-only list of time logged against one task, with the running total. */
function TimeEntriesCard({ task }: { task: Task }) {
  const entries = useQuery({
    queryKey: queryKeys.projects.timeEntries(task.id),
    queryFn: () => fetchTimeEntries(task.id),
  });

  if (entries.isError) {
    return <ErrorState message="Time entries didn't load." onRetry={entries.refetch} />;
  }
  const rows = entries.data ?? [];
  const totalHours = rows.reduce((sum, e) => sum + e.hours, 0);

  return (
    <DashboardCard title={`Time logged — ${task.title}`}>
      {rows.length === 0 ? (
        <EmptyState title="No time logged" action="Use “Log time” on the task to record hours." />
      ) : (
        <>
          <DataTable
            rows={rows}
            rowKey={(e) => e.id}
            columns={[
              { header: "Date", cell: (e) => shortDate(e.entryDate) },
              { header: "Who", cell: (e) => e.employeeName },
              { header: "Note", cell: (e) => e.note ?? "—" },
              { header: "Hours", align: "right", cell: (e) => e.hours.toFixed(2) },
            ]}
          />
          <p className="mt-3 text-right text-sm text-neutral-500">
            Total <span className="font-semibold tabular-nums text-neutral-900">{totalHours.toFixed(2)} h</span>
          </p>
        </>
      )}
    </DashboardCard>
  );
}

function NewTaskModal({ projectId, onClose, onCreated }: { projectId: number; onClose: () => void; onCreated: () => void }) {
  const { data: employees } = useQuery({ queryKey: queryKeys.hr.employees, queryFn: fetchEmployees });
  const [title, setTitle] = useState("");
  const [assigneeEmployeeId, setAssigneeEmployeeId] = useState<number | "">("");
  const [dueDate, setDueDate] = useState("");
  const [estimateHours, setEstimateHours] = useState("");

  const create = useMutation({
    mutationFn: () =>
      createTask({
        projectId,
        title,
        assigneeEmployeeId: assigneeEmployeeId === "" ? null : assigneeEmployeeId,
        dueDate: dueDate || null,
        estimateHours: estimateHours === "" ? null : Number(estimateHours),
      }),
    onSuccess: () => {
      onCreated();
      onClose();
    },
  });
  const error = create.error instanceof ApiError ? create.error.message : null;

  return (
    <Modal title="New task" onClose={onClose}>
      <form className="space-y-4" onSubmit={(e) => { e.preventDefault(); create.mutate(); }}>
        <label className="block text-sm">
          <span className="text-neutral-600">Title</span>
          <input required value={title} onChange={(e) => setTitle(e.target.value)} className="mt-1 w-full rounded border border-neutral-300 px-2 py-1" />
        </label>
        <label className="block text-sm">
          <span className="text-neutral-600">Assignee (optional)</span>
          <select
            value={assigneeEmployeeId}
            onChange={(e) => setAssigneeEmployeeId(e.target.value === "" ? "" : Number(e.target.value))}
            className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
          >
            <option value="">— unassigned —</option>
            {employees?.filter((emp) => emp.status !== "TERMINATED").map((emp) => (
              <option key={emp.id} value={emp.id}>{emp.name}</option>
            ))}
          </select>
        </label>
        <div className="grid grid-cols-2 gap-3">
          <label className="block text-sm">
            <span className="text-neutral-600">Due date</span>
            <input type="date" value={dueDate} onChange={(e) => setDueDate(e.target.value)} className="mt-1 w-full rounded border border-neutral-300 px-2 py-1" />
          </label>
          <label className="block text-sm">
            <span className="text-neutral-600">Estimate (hours)</span>
            <input type="number" min="0" step="1" value={estimateHours} onChange={(e) => setEstimateHours(e.target.value)} className="mt-1 w-full rounded border border-neutral-300 px-2 py-1 text-right tabular-nums" />
          </label>
        </div>
        {error && <p className="text-sm text-negative">{error}</p>}
        <button type="submit" disabled={create.isPending || title.trim() === ""} className="w-full rounded bg-projects px-3 py-2 text-sm font-medium text-white disabled:opacity-50">
          {create.isPending ? "Adding…" : "Add task"}
        </button>
      </form>
    </Modal>
  );
}

function NewMilestoneModal({ projectId, onClose, onCreated }: { projectId: number; onClose: () => void; onCreated: () => void }) {
  const [name, setName] = useState("");
  const [dueDate, setDueDate] = useState("");
  const create = useMutation({
    mutationFn: () => createMilestone({ projectId, name, dueDate: dueDate || null }),
    onSuccess: () => {
      onCreated();
      onClose();
    },
  });
  const error = create.error instanceof ApiError ? create.error.message : null;

  return (
    <Modal title="New milestone" onClose={onClose}>
      <form className="space-y-4" onSubmit={(e) => { e.preventDefault(); create.mutate(); }}>
        <label className="block text-sm">
          <span className="text-neutral-600">Name</span>
          <input required value={name} onChange={(e) => setName(e.target.value)} className="mt-1 w-full rounded border border-neutral-300 px-2 py-1" />
        </label>
        <label className="block text-sm">
          <span className="text-neutral-600">Due date</span>
          <input type="date" value={dueDate} onChange={(e) => setDueDate(e.target.value)} className="mt-1 w-full rounded border border-neutral-300 px-2 py-1" />
        </label>
        {error && <p className="text-sm text-negative">{error}</p>}
        <button type="submit" disabled={create.isPending || name.trim() === ""} className="w-full rounded bg-projects px-3 py-2 text-sm font-medium text-white disabled:opacity-50">
          {create.isPending ? "Adding…" : "Add milestone"}
        </button>
      </form>
    </Modal>
  );
}

function LogTimeModal({ task, onClose, onLogged }: { task: Task; onClose: () => void; onLogged: () => void }) {
  const { data: employees } = useQuery({ queryKey: queryKeys.hr.employees, queryFn: fetchEmployees });
  const [employeeId, setEmployeeId] = useState<number | "">(task.assigneeEmployeeId ?? "");
  const [entryDate, setEntryDate] = useState(todayIso());
  const [hours, setHours] = useState(8);
  const [note, setNote] = useState("");

  const log = useMutation({
    mutationFn: (body: TimeEntryRequest) => logTime(body),
    onSuccess: () => {
      onLogged();
      onClose();
    },
  });
  const error = log.error instanceof ApiError ? log.error.message : null;

  return (
    <Modal title={`Log time — ${task.title}`} onClose={onClose}>
      <form
        className="space-y-4"
        onSubmit={(e) => {
          e.preventDefault();
          if (employeeId === "") return;
          log.mutate({ taskId: task.id, employeeId, entryDate, hours, note: note || null });
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
            <option value="" disabled>Select…</option>
            {employees?.filter((emp) => emp.status !== "TERMINATED").map((emp) => (
              <option key={emp.id} value={emp.id}>{emp.name}</option>
            ))}
          </select>
        </label>
        <div className="grid grid-cols-2 gap-3">
          <label className="block text-sm">
            <span className="text-neutral-600">Date</span>
            <input type="date" value={entryDate} onChange={(e) => setEntryDate(e.target.value)} className="mt-1 w-full rounded border border-neutral-300 px-2 py-1" />
          </label>
          <label className="block text-sm">
            <span className="text-neutral-600">Hours (≤ 24)</span>
            <input type="number" min="0.25" max="24" step="0.25" value={hours} onChange={(e) => setHours(Number(e.target.value))} className="mt-1 w-full rounded border border-neutral-300 px-2 py-1 text-right tabular-nums" />
          </label>
        </div>
        <label className="block text-sm">
          <span className="text-neutral-600">Note</span>
          <input value={note} onChange={(e) => setNote(e.target.value)} className="mt-1 w-full rounded border border-neutral-300 px-2 py-1" />
        </label>
        {error && <p className="text-sm text-negative">{error}</p>}
        <button type="submit" disabled={log.isPending || employeeId === ""} className="w-full rounded bg-projects px-3 py-2 text-sm font-medium text-white disabled:opacity-50">
          {log.isPending ? "Logging…" : "Log time"}
        </button>
      </form>
    </Modal>
  );
}
