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
  createBill,
  fetchBills,
  fetchVendors,
  recordBillPayment,
  sendBill,
  voidBill,
} from "./api";
import type { Bill, BillRequest, LineItemRequest, PaymentRequest } from "./types";

const todayIso = () => new Date().toISOString().slice(0, 10);
const plusDaysIso = (days: number) =>
  new Date(Date.now() + days * 86_400_000).toISOString().slice(0, 10);

/** AP bills: list, create, and drive the lifecycle (send, record payment, void). */
export function BillsPage() {
  const qc = useQueryClient();
  const [showCreate, setShowCreate] = useState(false);
  const [payTarget, setPayTarget] = useState<Bill | null>(null);

  const { data, isPending, isError, refetch } = useQuery({
    queryKey: queryKeys.finance.bills,
    queryFn: fetchBills,
  });

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: queryKeys.finance.bills });
    qc.invalidateQueries({ queryKey: queryKeys.finance.dashboard });
  };

  const send = useMutation({ mutationFn: sendBill, onSuccess: invalidate });
  const cancel = useMutation({ mutationFn: voidBill, onSuccess: invalidate });

  if (isPending) return <DashboardSkeleton />;
  if (isError) return <ErrorState message="Bills didn't load." onRetry={refetch} />;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <PageHeader module="Bills (AP)" asOf={new Date()} />
        <button
          onClick={() => setShowCreate(true)}
          className="rounded bg-finance px-3 py-2 text-sm font-medium text-white"
        >
          New bill
        </button>
      </div>

      <DashboardCard title="All bills">
        {data.content.length === 0 ? (
          <EmptyState
            title="No bills yet"
            action="Record what you owe a vendor with “New bill.”"
          />
        ) : (
          <DataTable
            rows={data.content}
            rowKey={(r) => r.id}
            columns={[
              {
                header: "#",
                cell: (r) => (
                  <Link to={`/finance/bills/${r.id}`} className="font-medium text-finance hover:underline">
                    {r.id}
                  </Link>
                ),
              },
              { header: "Vendor", cell: (r) => r.vendorName },
              { header: "Issued", align: "right", cell: (r) => shortDate(r.issueDate) },
              { header: "Due", align: "right", cell: (r) => shortDate(r.dueDate) },
              {
                header: "Status",
                cell: (r) => <StatusBadge status={r.overdue ? "OVERDUE" : r.status} />,
              },
              { header: "Total", align: "right", cell: (r) => money(r.total, r.currency) },
              { header: "Outstanding", align: "right", cell: (r) => money(r.outstanding, r.currency) },
              { header: "", align: "right", cell: (r) => <RowActions bill={r} /> },
            ]}
          />
        )}
      </DashboardCard>

      {showCreate && <CreateBillModal onClose={() => setShowCreate(false)} onCreated={invalidate} />}
      {payTarget && (
        <PayBillModal bill={payTarget} onClose={() => setPayTarget(null)} onPaid={invalidate} />
      )}
    </div>
  );

  function RowActions({ bill }: { bill: Bill }) {
    const actions = [];
    if (bill.status === "DRAFT") {
      actions.push(
        <button key="send" className="text-finance hover:underline" onClick={() => send.mutate(bill.id)}>
          Approve
        </button>,
      );
    }
    if (bill.status === "SENT" || bill.status === "PARTIALLY_PAID") {
      actions.push(
        <button key="pay" className="text-finance hover:underline" onClick={() => setPayTarget(bill)}>
          Record payment
        </button>,
      );
    }
    if (bill.status === "DRAFT" || bill.status === "SENT") {
      actions.push(
        <button key="void" className="text-negative hover:underline" onClick={() => cancel.mutate(bill.id)}>
          Void
        </button>,
      );
    }
    return <div className="flex justify-end gap-3 text-sm font-medium">{actions}</div>;
  }
}

/** Modal wrapping the new-bill form. */
function CreateBillModal({ onClose, onCreated }: { onClose: () => void; onCreated: () => void }) {
  const { data: vendors } = useQuery({
    queryKey: queryKeys.finance.vendors,
    queryFn: fetchVendors,
  });
  const [vendorId, setVendorId] = useState<number | "">("");
  const [issueDate, setIssueDate] = useState(todayIso());
  const [dueDate, setDueDate] = useState(plusDaysIso(30));
  const [lines, setLines] = useState<LineItemRequest[]>([
    { description: "", quantity: 1, unitPrice: 0, currency: "USD" },
  ]);

  const create = useMutation({
    mutationFn: (body: BillRequest) => createBill(body),
    onSuccess: () => {
      onCreated();
      onClose();
    },
  });
  const error = create.error instanceof ApiError ? create.error.message : null;

  return (
    <Modal title="New bill" onClose={onClose}>
      <form
        className="space-y-4"
        onSubmit={(e) => {
          e.preventDefault();
          if (vendorId === "") return;
          create.mutate({ vendorId, issueDate, dueDate, lines });
        }}
      >
        <label className="block text-sm">
          <span className="text-neutral-600">Vendor</span>
          <select
            required
            value={vendorId}
            onChange={(e) => setVendorId(e.target.value === "" ? "" : Number(e.target.value))}
            className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
          >
            <option value="" disabled>
              Select a vendor…
            </option>
            {vendors?.map((v) => (
              <option key={v.id} value={v.id}>
                {v.name}
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
          disabled={create.isPending || vendorId === ""}
          className="w-full rounded bg-finance px-3 py-2 text-sm font-medium text-white disabled:opacity-50"
        >
          {create.isPending ? "Creating…" : "Create draft bill"}
        </button>
      </form>
    </Modal>
  );
}

/** Modal wrapping the record-payment form for a bill. */
function PayBillModal({
  bill,
  onClose,
  onPaid,
}: {
  bill: Bill;
  onClose: () => void;
  onPaid: () => void;
}) {
  const pay = useMutation({
    mutationFn: (body: PaymentRequest) => recordBillPayment(bill.id, body),
    onSuccess: () => {
      onPaid();
      onClose();
    },
  });
  const error = pay.error instanceof ApiError ? pay.error.message : null;

  return (
    <Modal title={`Record payment — bill #${bill.id}`} onClose={onClose}>
      <PaymentForm
        outstanding={bill.outstanding}
        currency={bill.currency}
        onSubmit={(body) => pay.mutate(body)}
        pending={pay.isPending}
        error={error}
      />
    </Modal>
  );
}
