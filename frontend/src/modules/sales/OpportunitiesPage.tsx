import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "../../lib/queryKeys";
import { money, percent, shortDate } from "../../lib/format";
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
  advanceOpportunity,
  createOpportunity,
  fetchOpportunities,
  loseOpportunity,
  reopenOpportunity,
  winOpportunity,
} from "./api";
import type { Opportunity, OpportunityRequest, OpportunityStage } from "./types";

const OPEN_STAGES: OpportunityStage[] = ["PROSPECTING", "QUALIFIED", "PROPOSAL", "NEGOTIATION"];
const plusDaysIso = (d: number) => new Date(Date.now() + d * 86_400_000).toISOString().slice(0, 10);

/** Opportunities: create, advance through stages, win, lose, reopen. */
export function OpportunitiesPage() {
  const qc = useQueryClient();
  const [showCreate, setShowCreate] = useState(false);
  const [loseTarget, setLoseTarget] = useState<Opportunity | null>(null);

  const { data, isPending, isError, refetch } = useQuery({
    queryKey: queryKeys.sales.opportunities,
    queryFn: fetchOpportunities,
  });

  /** Win/lose/advance ripple into the dashboard, rep rollup, and (on win) orders. */
  const invalidate = () => {
    qc.invalidateQueries({ queryKey: queryKeys.sales.opportunities });
    qc.invalidateQueries({ queryKey: queryKeys.sales.orders });
    qc.invalidateQueries({ queryKey: queryKeys.sales.dashboard });
    qc.invalidateQueries({ queryKey: ["sales", "reps"] });
    qc.invalidateQueries({ queryKey: ["sales", "closed-deals"] });
  };

  const win = useMutation({ mutationFn: winOpportunity, onSuccess: invalidate });
  const reopen = useMutation({ mutationFn: reopenOpportunity, onSuccess: invalidate });
  const advance = useMutation({
    mutationFn: (v: { id: number; stage: OpportunityStage }) => advanceOpportunity(v.id, v.stage),
    onSuccess: invalidate,
  });

  if (isPending) return <DashboardSkeleton />;
  if (isError) return <ErrorState message="Opportunities didn't load." onRetry={refetch} />;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <PageHeader module="Opportunities" asOf={new Date()} />
        <button onClick={() => setShowCreate(true)} className="rounded bg-sales px-3 py-2 text-sm font-medium text-white">
          New opportunity
        </button>
      </div>

      <DashboardCard title="Pipeline">
        {data.length === 0 ? (
          <EmptyState title="No opportunities yet" action="Qualify a lead or add one directly with “New opportunity.”" />
        ) : (
          <DataTable
            rows={data}
            rowKey={(r) => r.id}
            columns={[
              { header: "Customer", cell: (r) => r.customerName },
              { header: "Owner", cell: (r) => r.ownerName },
              { header: "Stage", cell: (r) => <StatusBadge status={r.stage} /> },
              { header: "Value", align: "right", cell: (r) => money(r.expectedValue, r.currency) },
              { header: "Weighted", align: "right", cell: (r) => money(r.weightedValue, r.currency) },
              { header: "Prob.", align: "right", cell: (r) => percent(r.probability / 100) },
              { header: "Close", align: "right", cell: (r) => shortDate(r.expectedCloseDate) },
              { header: "", align: "right", cell: (r) => <RowActions o={r} /> },
            ]}
          />
        )}
      </DashboardCard>

      {showCreate && <CreateOpportunityModal onClose={() => setShowCreate(false)} onCreated={invalidate} />}
      {loseTarget && (
        <LoseModal
          opportunity={loseTarget}
          onClose={() => setLoseTarget(null)}
          onLost={invalidate}
        />
      )}
    </div>
  );

  function RowActions({ o }: { o: Opportunity }) {
    if (o.stage === "WON") {
      return <span className="text-sm text-neutral-400">{o.salesOrderId ? `Order #${o.salesOrderId}` : "Won"}</span>;
    }
    if (o.stage === "LOST") {
      return (
        <button className="text-sm font-medium text-sales hover:underline" onClick={() => reopen.mutate(o.id)}>
          Reopen
        </button>
      );
    }
    const nextStages = OPEN_STAGES.filter((s) => OPEN_STAGES.indexOf(s) > OPEN_STAGES.indexOf(o.stage));
    return (
      <div className="flex items-center justify-end gap-3 text-sm font-medium">
        {nextStages.length > 0 && (
          <select
            defaultValue=""
            onChange={(e) => e.target.value && advance.mutate({ id: o.id, stage: e.target.value as OpportunityStage })}
            className="rounded border border-neutral-300 px-1 py-0.5 text-xs"
          >
            <option value="" disabled>
              Advance…
            </option>
            {nextStages.map((s) => (
              <option key={s} value={s}>
                {s.toLowerCase()}
              </option>
            ))}
          </select>
        )}
        <button className="text-positive hover:underline" onClick={() => win.mutate(o.id)}>
          Win
        </button>
        <button className="text-negative hover:underline" onClick={() => setLoseTarget(o)}>
          Lose
        </button>
      </div>
    );
  }
}

/** New-opportunity form for an existing customer. */
function CreateOpportunityModal({ onClose, onCreated }: { onClose: () => void; onCreated: () => void }) {
  const { data: customers } = useQuery({ queryKey: queryKeys.finance.customers, queryFn: fetchCustomers });
  const { data: employees } = useQuery({ queryKey: queryKeys.hr.employees, queryFn: fetchEmployees });
  const [customerId, setCustomerId] = useState<number | "">("");
  const [ownerEmployeeId, setOwnerEmployeeId] = useState<number | "">("");
  const [expectedValue, setExpectedValue] = useState(10000);
  const [probability, setProbability] = useState(50);
  const [expectedCloseDate, setExpectedCloseDate] = useState(plusDaysIso(30));

  const create = useMutation({
    mutationFn: (body: OpportunityRequest) => createOpportunity(body),
    onSuccess: () => {
      onCreated();
      onClose();
    },
  });
  const error = create.error instanceof ApiError ? create.error.message : null;

  return (
    <Modal title="New opportunity" onClose={onClose}>
      <form
        className="space-y-4"
        onSubmit={(e) => {
          e.preventDefault();
          if (customerId === "" || ownerEmployeeId === "") return;
          create.mutate({
            customerId,
            ownerEmployeeId,
            expectedValue,
            currency: "USD",
            probability,
            expectedCloseDate,
          });
        }}
      >
        <label className="block text-sm">
          <span className="text-neutral-600">Customer</span>
          <select
            required
            value={customerId}
            onChange={(e) => setCustomerId(e.target.value === "" ? "" : Number(e.target.value))}
            className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
          >
            <option value="" disabled>
              Select a customer…
            </option>
            {customers?.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </label>
        <label className="block text-sm">
          <span className="text-neutral-600">Salesperson (owner)</span>
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
        </label>
        <div className="grid grid-cols-3 gap-3">
          <label className="block text-sm">
            <span className="text-neutral-600">Value (USD)</span>
            <input
              type="number"
              min="0"
              step="100"
              value={expectedValue}
              onChange={(e) => setExpectedValue(Number(e.target.value))}
              className="mt-1 w-full rounded border border-neutral-300 px-2 py-1 text-right tabular-nums"
            />
          </label>
          <label className="block text-sm">
            <span className="text-neutral-600">Prob. %</span>
            <input
              type="number"
              min="0"
              max="100"
              value={probability}
              onChange={(e) => setProbability(Number(e.target.value))}
              className="mt-1 w-full rounded border border-neutral-300 px-2 py-1 text-right tabular-nums"
            />
          </label>
          <label className="block text-sm">
            <span className="text-neutral-600">Close</span>
            <input
              type="date"
              value={expectedCloseDate}
              onChange={(e) => setExpectedCloseDate(e.target.value)}
              className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
            />
          </label>
        </div>
        {error && <p className="text-sm text-negative">{error}</p>}
        <button
          type="submit"
          disabled={create.isPending || customerId === "" || ownerEmployeeId === ""}
          className="w-full rounded bg-sales px-3 py-2 text-sm font-medium text-white disabled:opacity-50"
        >
          {create.isPending ? "Creating…" : "Create opportunity"}
        </button>
      </form>
    </Modal>
  );
}

/** Lose form: a reason is required. */
function LoseModal({
  opportunity,
  onClose,
  onLost,
}: {
  opportunity: Opportunity;
  onClose: () => void;
  onLost: () => void;
}) {
  const [reason, setReason] = useState("");
  const lose = useMutation({
    mutationFn: (lostReason: string) => loseOpportunity(opportunity.id, lostReason),
    onSuccess: () => {
      onLost();
      onClose();
    },
  });
  const error = lose.error instanceof ApiError ? lose.error.message : null;

  return (
    <Modal title={`Mark lost — ${opportunity.customerName}`} onClose={onClose}>
      <form
        className="space-y-4"
        onSubmit={(e) => {
          e.preventDefault();
          if (reason.trim()) lose.mutate(reason);
        }}
      >
        <label className="block text-sm">
          <span className="text-neutral-600">Reason</span>
          <input
            required
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="e.g. Budget cut, chose competitor"
            className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
          />
        </label>
        {error && <p className="text-sm text-negative">{error}</p>}
        <button
          type="submit"
          disabled={lose.isPending || reason.trim() === ""}
          className="w-full rounded bg-negative px-3 py-2 text-sm font-medium text-white disabled:opacity-50"
        >
          {lose.isPending ? "Saving…" : "Mark lost"}
        </button>
      </form>
    </Modal>
  );
}
