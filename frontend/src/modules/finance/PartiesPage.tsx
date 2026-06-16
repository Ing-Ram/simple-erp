import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "../../lib/queryKeys";
import { count } from "../../lib/format";
import { ApiError } from "../../lib/api";
import { PageHeader } from "../../components/PageHeader";
import { DashboardCard } from "../../components/DashboardCard";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { ErrorState } from "../../components/ErrorState";
import { EmptyState } from "../../components/EmptyState";
import { DashboardSkeleton } from "../../components/DashboardSkeleton";
import { Modal } from "../../components/Modal";
import { createCustomer, createVendor, fetchCustomers, fetchVendors } from "./api";
import type { Customer, PartyRequest, Vendor } from "./types";

type Party = Customer | Vendor;

const CONFIG = {
  customers: {
    title: "Customers",
    queryKey: queryKeys.finance.customers,
    fetch: fetchCustomers,
    create: createCustomer,
    empty: "Add a customer to invoice, or qualify a sales lead.",
  },
  vendors: {
    title: "Vendors",
    queryKey: queryKeys.finance.vendors,
    fetch: fetchVendors,
    create: createVendor,
    empty: "Add a vendor to record bills against.",
  },
} as const;

/** Customers or Vendors: list and create. Both parties share the same shape. */
export function PartiesPage({ kind }: { kind: "customers" | "vendors" }) {
  const cfg = CONFIG[kind];
  const qc = useQueryClient();
  const [showCreate, setShowCreate] = useState(false);

  const { data, isPending, isError, refetch } = useQuery({
    queryKey: cfg.queryKey,
    queryFn: cfg.fetch,
  });

  if (isPending) return <DashboardSkeleton />;
  if (isError) return <ErrorState message={`${cfg.title} didn't load.`} onRetry={refetch} />;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <PageHeader module={cfg.title} asOf={new Date()} />
        <button onClick={() => setShowCreate(true)} className="rounded bg-finance px-3 py-2 text-sm font-medium text-white">
          New {kind === "customers" ? "customer" : "vendor"}
        </button>
      </div>

      <DashboardCard title={`All ${kind}`}>
        {data.length === 0 ? (
          <EmptyState title={`No ${kind} yet`} action={cfg.empty} />
        ) : (
          <DataTable
            rows={data as Party[]}
            rowKey={(r) => r.id}
            columns={[
              { header: "Name", cell: (r) => r.name },
              { header: "Email", cell: (r) => r.email ?? "—" },
              { header: "Terms", align: "right", cell: (r) => `${count(r.paymentTermsDays)} days` },
              { header: "Status", cell: (r) => <StatusBadge status={r.active ? "ACTIVE" : "INACTIVE"} /> },
            ]}
          />
        )}
      </DashboardCard>

      {showCreate && (
        <CreatePartyModal
          kind={kind}
          create={cfg.create}
          onClose={() => setShowCreate(false)}
          onCreated={() => qc.invalidateQueries({ queryKey: cfg.queryKey })}
        />
      )}
    </div>
  );
}

/** Shared create form for a customer or vendor. */
function CreatePartyModal({
  kind,
  create,
  onClose,
  onCreated,
}: {
  kind: "customers" | "vendors";
  create: (body: PartyRequest) => Promise<Party>;
  onClose: () => void;
  onCreated: () => void;
}) {
  const [form, setForm] = useState<PartyRequest>({ name: "", email: "", paymentTermsDays: 30 });
  const mutation = useMutation({
    mutationFn: (body: PartyRequest) => create(body),
    onSuccess: () => {
      onCreated();
      onClose();
    },
  });
  const error = mutation.error instanceof ApiError ? mutation.error.message : null;
  const singular = kind === "customers" ? "customer" : "vendor";

  return (
    <Modal title={`New ${singular}`} onClose={onClose}>
      <form
        className="space-y-4"
        onSubmit={(e) => {
          e.preventDefault();
          mutation.mutate(form);
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
          <span className="text-neutral-600">Payment terms (days)</span>
          <input
            type="number"
            min="0"
            value={form.paymentTermsDays}
            onChange={(e) => setForm({ ...form, paymentTermsDays: Number(e.target.value) })}
            className="mt-1 w-full rounded border border-neutral-300 px-2 py-1 text-right tabular-nums"
          />
        </label>
        {error && <p className="text-sm text-negative">{error}</p>}
        <button
          type="submit"
          disabled={mutation.isPending || form.name.trim() === ""}
          className="w-full rounded bg-finance px-3 py-2 text-sm font-medium text-white disabled:opacity-50"
        >
          {mutation.isPending ? "Saving…" : `Create ${singular}`}
        </button>
      </form>
    </Modal>
  );
}
