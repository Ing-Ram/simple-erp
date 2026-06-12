import { useState } from "react";
import { Link } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "../../lib/queryKeys";
import { money, shortDate } from "../../lib/format";
import { ApiError } from "../../lib/api";
import { PageHeader } from "../../components/PageHeader";
import { DashboardCard } from "../../components/DashboardCard";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { ErrorState } from "../../components/ErrorState";
import { EmptyState } from "../../components/EmptyState";
import { DashboardSkeleton } from "../../components/DashboardSkeleton";
import { Modal } from "../../components/Modal";
import { LineItemsEditor } from "./components/LineItemsEditor";
import { PaymentForm } from "./components/PaymentForm";
import {
  createInvoice,
  fetchCustomers,
  fetchInvoices,
  recordInvoicePayment,
  sendInvoice,
  voidInvoice,
} from "./api";
import type { Invoice, InvoiceRequest, LineItemRequest, PaymentRequest } from "./types";

const todayIso = () => new Date().toISOString().slice(0, 10);
const plusDaysIso = (days: number) =>
  new Date(Date.now() + days * 86_400_000).toISOString().slice(0, 10);

/** AR invoices: list, create, and drive the lifecycle (send, record payment, void). */
export function InvoicesPage() {
  const qc = useQueryClient();
  const [showCreate, setShowCreate] = useState(false);
  const [payTarget, setPayTarget] = useState<Invoice | null>(null);

  const { data, isPending, isError, refetch } = useQuery({
    queryKey: queryKeys.finance.invoices,
    queryFn: fetchInvoices,
  });

  /** Invalidate both the list and the dashboard after any mutation. */
  const invalidate = () => {
    qc.invalidateQueries({ queryKey: queryKeys.finance.invoices });
    qc.invalidateQueries({ queryKey: queryKeys.finance.dashboard });
  };

  const send = useMutation({ mutationFn: sendInvoice, onSuccess: invalidate });
  const cancel = useMutation({ mutationFn: voidInvoice, onSuccess: invalidate });

  if (isPending) return <DashboardSkeleton />;
  if (isError) return <ErrorState message="Invoices didn't load." onRetry={refetch} />;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <PageHeader module="Invoices (AR)" asOf={new Date()} />
        <button
          onClick={() => setShowCreate(true)}
          className="rounded bg-finance px-3 py-2 text-sm font-medium text-white"
        >
          New invoice
        </button>
      </div>

      <DashboardCard title="All invoices">
        {data.content.length === 0 ? (
          <EmptyState
            title="No invoices yet"
            action="Create one from a sales order or add one directly with “New invoice.”"
          />
        ) : (
          <DataTable
            rows={data.content}
            rowKey={(r) => r.id}
            columns={[
              {
                header: "#",
                cell: (r) => (
                  <Link to={`/finance/invoices/${r.id}`} className="font-medium text-finance hover:underline">
                    {r.id}
                  </Link>
                ),
              },
              { header: "Customer", cell: (r) => r.customerName },
              { header: "Issued", align: "right", cell: (r) => shortDate(r.issueDate) },
              { header: "Due", align: "right", cell: (r) => shortDate(r.dueDate) },
              {
                header: "Status",
                cell: (r) => <StatusBadge status={r.overdue ? "OVERDUE" : r.status} />,
              },
              { header: "Total", align: "right", cell: (r) => money(r.total, r.currency) },
              { header: "Outstanding", align: "right", cell: (r) => money(r.outstanding, r.currency) },
              { header: "", align: "right", cell: (r) => <RowActions invoice={r} /> },
            ]}
          />
        )}
      </DashboardCard>

      {showCreate && <CreateInvoiceModal onClose={() => setShowCreate(false)} onCreated={invalidate} />}
      {payTarget && (
        <PayInvoiceModal invoice={payTarget} onClose={() => setPayTarget(null)} onPaid={invalidate} />
      )}
    </div>
  );

  /** Status-aware action buttons for one row. */
  function RowActions({ invoice }: { invoice: Invoice }) {
    const actions = [];
    if (invoice.status === "DRAFT") {
      actions.push(
        <button key="send" className="text-finance hover:underline" onClick={() => send.mutate(invoice.id)}>
          Send
        </button>,
      );
    }
    if (invoice.status === "SENT" || invoice.status === "PARTIALLY_PAID") {
      actions.push(
        <button key="pay" className="text-finance hover:underline" onClick={() => setPayTarget(invoice)}>
          Record payment
        </button>,
      );
    }
    if (invoice.status === "DRAFT" || invoice.status === "SENT") {
      actions.push(
        <button key="void" className="text-negative hover:underline" onClick={() => cancel.mutate(invoice.id)}>
          Void
        </button>,
      );
    }
    return <div className="flex justify-end gap-3 text-sm font-medium">{actions}</div>;
  }
}

/** Modal wrapping the new-invoice form. */
function CreateInvoiceModal({ onClose, onCreated }: { onClose: () => void; onCreated: () => void }) {
  const { data: customers } = useQuery({
    queryKey: queryKeys.finance.customers,
    queryFn: fetchCustomers,
  });
  const [customerId, setCustomerId] = useState<number | "">("");
  const [issueDate, setIssueDate] = useState(todayIso());
  const [dueDate, setDueDate] = useState(plusDaysIso(30));
  const [lines, setLines] = useState<LineItemRequest[]>([
    { description: "", quantity: 1, unitPrice: 0, currency: "USD" },
  ]);

  const create = useMutation({
    mutationFn: (body: InvoiceRequest) => createInvoice(body),
    onSuccess: () => {
      onCreated();
      onClose();
    },
  });
  const error = create.error instanceof ApiError ? create.error.message : null;

  return (
    <Modal title="New invoice" onClose={onClose}>
      <form
        className="space-y-4"
        onSubmit={(e) => {
          e.preventDefault();
          if (customerId === "") return;
          create.mutate({ customerId, issueDate, dueDate, lines });
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

        <div className="grid grid-cols-2 gap-3">
          <label className="block text-sm">
            <span className="text-neutral-600">Issue date</span>
            <input
              type="date"
              required
              value={issueDate}
              onChange={(e) => setIssueDate(e.target.value)}
              className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
            />
          </label>
          <label className="block text-sm">
            <span className="text-neutral-600">Due date</span>
            <input
              type="date"
              required
              value={dueDate}
              onChange={(e) => setDueDate(e.target.value)}
              className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
            />
          </label>
        </div>

        <LineItemsEditor lines={lines} onChange={setLines} />

        {error && <p className="text-sm text-negative">{error}</p>}

        <button
          type="submit"
          disabled={create.isPending || customerId === ""}
          className="w-full rounded bg-finance px-3 py-2 text-sm font-medium text-white disabled:opacity-50"
        >
          {create.isPending ? "Creating…" : "Create draft invoice"}
        </button>
      </form>
    </Modal>
  );
}

/** Modal wrapping the record-payment form for an invoice. */
function PayInvoiceModal({
  invoice,
  onClose,
  onPaid,
}: {
  invoice: Invoice;
  onClose: () => void;
  onPaid: () => void;
}) {
  const pay = useMutation({
    mutationFn: (body: PaymentRequest) => recordInvoicePayment(invoice.id, body),
    onSuccess: () => {
      onPaid();
      onClose();
    },
  });
  const error = pay.error instanceof ApiError ? pay.error.message : null;

  return (
    <Modal title={`Record payment — invoice #${invoice.id}`} onClose={onClose}>
      <PaymentForm
        outstanding={invoice.outstanding}
        currency={invoice.currency}
        onSubmit={(body) => pay.mutate(body)}
        pending={pay.isPending}
        error={error}
      />
    </Modal>
  );
}
