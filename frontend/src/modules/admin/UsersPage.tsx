import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "../../lib/queryKeys";
import { ApiError } from "../../lib/api";
import type { AuthUser } from "../../lib/auth";
import { PageHeader } from "../../components/PageHeader";
import { DashboardCard } from "../../components/DashboardCard";
import { DataTable } from "../../components/DataTable";
import { ErrorState } from "../../components/ErrorState";
import { EmptyState } from "../../components/EmptyState";
import { DashboardSkeleton } from "../../components/DashboardSkeleton";
import { Modal } from "../../components/Modal";
import {
  changeUserRole,
  createUser,
  fetchUsers,
  resetUserPassword,
  type CreateUserRequest,
  type ManagedUser,
} from "./api";

const ROLES: AuthUser["role"][] = ["ADMIN", "MEMBER"];

/** Admin-only user management: list accounts, create, change role, reset password. */
export function UsersPage() {
  const qc = useQueryClient();
  const [showCreate, setShowCreate] = useState(false);
  const [resetTarget, setResetTarget] = useState<ManagedUser | null>(null);

  const { data, isPending, isError, refetch } = useQuery({
    queryKey: queryKeys.admin.users,
    queryFn: fetchUsers,
  });

  const invalidate = () => qc.invalidateQueries({ queryKey: queryKeys.admin.users });
  const role = useMutation({
    mutationFn: (v: { id: number; role: AuthUser["role"] }) => changeUserRole(v.id, v.role),
    onSuccess: invalidate,
  });

  if (isPending) return <DashboardSkeleton />;
  if (isError) return <ErrorState message="Users didn't load." onRetry={refetch} />;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <PageHeader module="Users" asOf={new Date()} />
        <button onClick={() => setShowCreate(true)} className="rounded bg-finance px-3 py-2 text-sm font-medium text-white">
          New user
        </button>
      </div>

      <DashboardCard title="All users">
        {data.length === 0 ? (
          <EmptyState title="No users" action="Create the first account with “New user.”" />
        ) : (
          <DataTable
            rows={data}
            rowKey={(u) => u.id}
            columns={[
              { header: "Username", cell: (u) => u.username },
              { header: "Name", cell: (u) => u.displayName },
              {
                header: "Role",
                cell: (u) => (
                  <select
                    value={u.role}
                    onChange={(e) => role.mutate({ id: u.id, role: e.target.value as AuthUser["role"] })}
                    className="rounded border border-neutral-300 px-1 py-0.5 text-xs"
                  >
                    {ROLES.map((r) => (
                      <option key={r} value={r}>
                        {r.toLowerCase()}
                      </option>
                    ))}
                  </select>
                ),
              },
              {
                header: "",
                align: "right",
                cell: (u) => (
                  <button
                    className="text-sm font-medium text-neutral-600 hover:underline"
                    onClick={() => setResetTarget(u)}
                  >
                    Reset password
                  </button>
                ),
              },
            ]}
          />
        )}
      </DashboardCard>

      {showCreate && <CreateUserModal onClose={() => setShowCreate(false)} onCreated={invalidate} />}
      {resetTarget && <ResetPasswordModal user={resetTarget} onClose={() => setResetTarget(null)} />}
    </div>
  );
}

function CreateUserModal({ onClose, onCreated }: { onClose: () => void; onCreated: () => void }) {
  const [form, setForm] = useState<CreateUserRequest>({
    username: "",
    displayName: "",
    role: "MEMBER",
    password: "",
  });
  const create = useMutation({
    mutationFn: (body: CreateUserRequest) => createUser(body),
    onSuccess: () => {
      onCreated();
      onClose();
    },
  });
  const error = create.error instanceof ApiError ? create.error.message : null;

  return (
    <Modal title="New user" onClose={onClose}>
      <form className="space-y-4" onSubmit={(e) => { e.preventDefault(); create.mutate(form); }}>
        <label className="block text-sm">
          <span className="text-neutral-600">Username</span>
          <input
            required
            value={form.username}
            onChange={(e) => setForm({ ...form, username: e.target.value })}
            className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
          />
        </label>
        <label className="block text-sm">
          <span className="text-neutral-600">Display name</span>
          <input
            required
            value={form.displayName}
            onChange={(e) => setForm({ ...form, displayName: e.target.value })}
            className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
          />
        </label>
        <div className="grid grid-cols-2 gap-3">
          <label className="block text-sm">
            <span className="text-neutral-600">Role</span>
            <select
              value={form.role}
              onChange={(e) => setForm({ ...form, role: e.target.value as AuthUser["role"] })}
              className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
            >
              {ROLES.map((r) => (
                <option key={r} value={r}>
                  {r.toLowerCase()}
                </option>
              ))}
            </select>
          </label>
          <label className="block text-sm">
            <span className="text-neutral-600">Password (min 8)</span>
            <input
              type="password"
              required
              minLength={8}
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
            />
          </label>
        </div>
        {error && <p className="text-sm text-negative">{error}</p>}
        <button
          type="submit"
          disabled={create.isPending || form.username.trim() === "" || form.password.length < 8}
          className="w-full rounded bg-finance px-3 py-2 text-sm font-medium text-white disabled:opacity-50"
        >
          {create.isPending ? "Creating…" : "Create user"}
        </button>
      </form>
    </Modal>
  );
}

function ResetPasswordModal({ user, onClose }: { user: ManagedUser; onClose: () => void }) {
  const [password, setPassword] = useState("");
  const reset = useMutation({
    mutationFn: (p: string) => resetUserPassword(user.id, p),
    onSuccess: onClose,
  });
  const error = reset.error instanceof ApiError ? reset.error.message : null;

  return (
    <Modal title={`Reset password — ${user.username}`} onClose={onClose}>
      <form className="space-y-4" onSubmit={(e) => { e.preventDefault(); reset.mutate(password); }}>
        <label className="block text-sm">
          <span className="text-neutral-600">New password (min 8)</span>
          <input
            type="password"
            required
            minLength={8}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
          />
        </label>
        {error && <p className="text-sm text-negative">{error}</p>}
        <button
          type="submit"
          disabled={reset.isPending || password.length < 8}
          className="w-full rounded bg-finance px-3 py-2 text-sm font-medium text-white disabled:opacity-50"
        >
          {reset.isPending ? "Saving…" : "Set password"}
        </button>
      </form>
    </Modal>
  );
}
