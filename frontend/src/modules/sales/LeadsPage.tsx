import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "../../lib/queryKeys";
import { ApiError } from "../../lib/api";
import { PageHeader } from "../../components/PageHeader";
import { DashboardCard } from "../../components/DashboardCard";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { ErrorState } from "../../components/ErrorState";
import { EmptyState } from "../../components/EmptyState";
import { DashboardSkeleton } from "../../components/DashboardSkeleton";
import { Modal } from "../../components/Modal";
import { fetchEmployees } from "../hr/api";
import {
  contactLead,
  createLead,
  disqualifyLead,
  fetchLeads,
  qualifyLead,
} from "./api";
import type { Lead, LeadRequest, LeadSource, QualifyLeadRequest } from "./types";

const SOURCES: LeadSource[] = ["REFERRAL", "WEBSITE", "OUTBOUND", "EVENT"];
const plusDaysIso = (d: number) => new Date(Date.now() + d * 86_400_000).toISOString().slice(0, 10);

/** Leads: capture, contact, qualify (→ customer + opportunity), and disqualify. */
export function LeadsPage() {
  const qc = useQueryClient();
  const [showCreate, setShowCreate] = useState(false);
  const [qualifyTarget, setQualifyTarget] = useState<Lead | null>(null);

  const { data, isPending, isError, refetch } = useQuery({
    queryKey: queryKeys.sales.leads,
    queryFn: fetchLeads,
  });

  const invalidateLeads = () => qc.invalidateQueries({ queryKey: queryKeys.sales.leads });
  const contact = useMutation({ mutationFn: contactLead, onSuccess: invalidateLeads });
  const disqualify = useMutation({ mutationFn: disqualifyLead, onSuccess: invalidateLeads });

  if (isPending) return <DashboardSkeleton />;
  if (isError) return <ErrorState message="Leads didn't load." onRetry={refetch} />;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <PageHeader module="Leads" asOf={new Date()} />
        <button onClick={() => setShowCreate(true)} className="rounded bg-sales px-3 py-2 text-sm font-medium text-white">
          New lead
        </button>
      </div>

      <DashboardCard title="All leads">
        {data.length === 0 ? (
          <EmptyState title="No leads yet" action="Capture your first lead with “New lead.”" />
        ) : (
          <DataTable
            rows={data}
            rowKey={(r) => r.id}
            columns={[
              { header: "Name", cell: (r) => r.name },
              { header: "Company", cell: (r) => r.company ?? "—" },
              { header: "Source", cell: (r) => r.source.toLowerCase() },
              { header: "Status", cell: (r) => <StatusBadge status={r.status} /> },
              { header: "", align: "right", cell: (r) => <RowActions lead={r} /> },
            ]}
          />
        )}
      </DashboardCard>

      {showCreate && <CreateLeadModal onClose={() => setShowCreate(false)} onCreated={invalidateLeads} />}
      {qualifyTarget && (
        <QualifyModal
          lead={qualifyTarget}
          onClose={() => setQualifyTarget(null)}
          onQualified={() => {
            invalidateLeads();
            qc.invalidateQueries({ queryKey: queryKeys.sales.opportunities });
            qc.invalidateQueries({ queryKey: queryKeys.sales.dashboard });
          }}
        />
      )}
    </div>
  );

  function RowActions({ lead }: { lead: Lead }) {
    const canWork = lead.status === "NEW" || lead.status === "CONTACTED";
    return (
      <div className="flex justify-end gap-3 text-sm font-medium">
        {lead.status === "NEW" && (
          <button className="text-sales hover:underline" onClick={() => contact.mutate(lead.id)}>
            Contact
          </button>
        )}
        {canWork && (
          <button className="text-sales hover:underline" onClick={() => setQualifyTarget(lead)}>
            Qualify
          </button>
        )}
        {canWork && (
          <button className="text-negative hover:underline" onClick={() => disqualify.mutate(lead.id)}>
            Disqualify
          </button>
        )}
      </div>
    );
  }
}

/** New-lead form. */
function CreateLeadModal({ onClose, onCreated }: { onClose: () => void; onCreated: () => void }) {
  const [form, setForm] = useState<LeadRequest>({ name: "", company: "", email: "", source: "WEBSITE" });
  const create = useMutation({
    mutationFn: (body: LeadRequest) => createLead(body),
    onSuccess: () => {
      onCreated();
      onClose();
    },
  });
  const error = create.error instanceof ApiError ? create.error.message : null;

  return (
    <Modal title="New lead" onClose={onClose}>
      <form
        className="space-y-4"
        onSubmit={(e) => {
          e.preventDefault();
          create.mutate(form);
        }}
      >
        <Field label="Name">
          <input
            required
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
          />
        </Field>
        <Field label="Company">
          <input
            value={form.company}
            onChange={(e) => setForm({ ...form, company: e.target.value })}
            className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
          />
        </Field>
        <Field label="Email">
          <input
            type="email"
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
            className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
          />
        </Field>
        <Field label="Source">
          <select
            value={form.source}
            onChange={(e) => setForm({ ...form, source: e.target.value as LeadSource })}
            className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
          >
            {SOURCES.map((s) => (
              <option key={s} value={s}>
                {s.toLowerCase()}
              </option>
            ))}
          </select>
        </Field>
        {error && <p className="text-sm text-negative">{error}</p>}
        <button
          type="submit"
          disabled={create.isPending || form.name.trim() === ""}
          className="w-full rounded bg-sales px-3 py-2 text-sm font-medium text-white disabled:opacity-50"
        >
          {create.isPending ? "Saving…" : "Capture lead"}
        </button>
      </form>
    </Modal>
  );
}

/** Qualify form: picks an owner and the opportunity terms. */
function QualifyModal({
  lead,
  onClose,
  onQualified,
}: {
  lead: Lead;
  onClose: () => void;
  onQualified: () => void;
}) {
  const { data: employees } = useQuery({ queryKey: queryKeys.hr.employees, queryFn: fetchEmployees });
  const [ownerEmployeeId, setOwnerEmployeeId] = useState<number | "">("");
  const [expectedValue, setExpectedValue] = useState(10000);
  const [probability, setProbability] = useState(50);
  const [expectedCloseDate, setExpectedCloseDate] = useState(plusDaysIso(30));
  const [paymentTermsDays, setPaymentTermsDays] = useState(30);

  const qualify = useMutation({
    mutationFn: (body: QualifyLeadRequest) => qualifyLead(lead.id, body),
    onSuccess: () => {
      onQualified();
      onClose();
    },
  });
  const error = qualify.error instanceof ApiError ? qualify.error.message : null;

  return (
    <Modal title={`Qualify ${lead.name}`} onClose={onClose}>
      <form
        className="space-y-4"
        onSubmit={(e) => {
          e.preventDefault();
          if (ownerEmployeeId === "") return;
          qualify.mutate({
            ownerEmployeeId,
            expectedValue,
            currency: "USD",
            probability,
            expectedCloseDate,
            paymentTermsDays,
          });
        }}
      >
        <p className="text-sm text-neutral-500">
          Creates a customer for {lead.company || lead.name} and an opportunity owned by the salesperson below.
        </p>
        <Field label="Salesperson (owner)">
          <select
            required
            value={ownerEmployeeId}
            onChange={(e) => setOwnerEmployeeId(e.target.value === "" ? "" : Number(e.target.value))}
            className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
          >
            <option value="" disabled>
              Select a salesperson…
            </option>
            {employees
              ?.filter((emp) => emp.status !== "TERMINATED")
              .map((emp) => (
                <option key={emp.id} value={emp.id}>
                  {emp.name}
                </option>
              ))}
          </select>
        </Field>
        <div className="grid grid-cols-2 gap-3">
          <Field label="Expected value (USD)">
            <input
              type="number"
              min="0"
              step="100"
              value={expectedValue}
              onChange={(e) => setExpectedValue(Number(e.target.value))}
              className="mt-1 w-full rounded border border-neutral-300 px-2 py-1 text-right tabular-nums"
            />
          </Field>
          <Field label="Probability %">
            <input
              type="number"
              min="0"
              max="100"
              value={probability}
              onChange={(e) => setProbability(Number(e.target.value))}
              className="mt-1 w-full rounded border border-neutral-300 px-2 py-1 text-right tabular-nums"
            />
          </Field>
          <Field label="Expected close">
            <input
              type="date"
              value={expectedCloseDate}
              onChange={(e) => setExpectedCloseDate(e.target.value)}
              className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
            />
          </Field>
          <Field label="Payment terms (days)">
            <input
              type="number"
              min="0"
              value={paymentTermsDays}
              onChange={(e) => setPaymentTermsDays(Number(e.target.value))}
              className="mt-1 w-full rounded border border-neutral-300 px-2 py-1 text-right tabular-nums"
            />
          </Field>
        </div>
        {error && <p className="text-sm text-negative">{error}</p>}
        <button
          type="submit"
          disabled={qualify.isPending || ownerEmployeeId === ""}
          className="w-full rounded bg-sales px-3 py-2 text-sm font-medium text-white disabled:opacity-50"
        >
          {qualify.isPending ? "Qualifying…" : "Qualify lead"}
        </button>
      </form>
    </Modal>
  );
}

/** One labeled form field. */
function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <label className="block text-sm">
      <span className="text-neutral-600">{label}</span>
      {children}
    </label>
  );
}
