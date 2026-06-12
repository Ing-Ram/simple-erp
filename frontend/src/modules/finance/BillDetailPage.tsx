import { Link, useParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { queryKeys } from "../../lib/queryKeys";
import { money, shortDate } from "../../lib/format";
import { DashboardCard } from "../../components/DashboardCard";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { ErrorState } from "../../components/ErrorState";
import { DashboardSkeleton } from "../../components/DashboardSkeleton";
import { PaymentsHistory } from "./components/PaymentsHistory";
import { fetchBill, fetchBillPayments } from "./api";

/** Read-only detail for one AP bill: summary, lines, and payment history. */
export function BillDetailPage() {
  const id = Number(useParams().id);

  const bill = useQuery({
    queryKey: queryKeys.finance.bill(id),
    queryFn: () => fetchBill(id),
  });
  const payments = useQuery({
    queryKey: queryKeys.finance.billPayments(id),
    queryFn: () => fetchBillPayments(id),
  });

  if (bill.isPending) return <DashboardSkeleton />;
  if (bill.isError) {
    return <ErrorState message="That bill didn't load." onRetry={bill.refetch} />;
  }
  const b = bill.data;

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <Link to="/finance/bills" className="text-sm text-neutral-500 hover:text-neutral-900">
          ← Bills
        </Link>
        <h1 className="text-xl font-semibold tracking-tight">Bill #{b.id}</h1>
        <StatusBadge status={b.overdue ? "OVERDUE" : b.status} />
      </div>

      <DashboardCard title={b.vendorName}>
        <dl className="grid grid-cols-2 gap-x-8 gap-y-3 text-sm sm:grid-cols-3">
          <Field label="Issued" value={shortDate(b.issueDate)} />
          <Field label="Due" value={shortDate(b.dueDate)} />
          <Field label="Total" value={money(b.total, b.currency)} />
          <Field label="Paid" value={money(b.amountPaid, b.currency)} />
          <Field label="Outstanding" value={money(b.outstanding, b.currency)} emphasize />
        </dl>
      </DashboardCard>

      <DashboardCard title="Line items">
        <DataTable
          rows={b.lines}
          rowKey={(l) => l.id}
          columns={[
            { header: "Description", cell: (l) => l.description },
            { header: "Qty", align: "right", cell: (l) => l.quantity },
            { header: "Unit price", align: "right", cell: (l) => money(l.unitPrice, b.currency) },
            {
              header: "Line total",
              align: "right",
              cell: (l) => money(l.quantity * l.unitPrice, b.currency),
            },
          ]}
        />
      </DashboardCard>

      {payments.isError ? (
        <ErrorState message="Payment history didn't load." onRetry={payments.refetch} />
      ) : (
        <PaymentsHistory payments={payments.data ?? []} />
      )}
    </div>
  );
}

/** One labeled value in the summary grid. */
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
