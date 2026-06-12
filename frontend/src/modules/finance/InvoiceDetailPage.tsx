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
import { fetchInvoice, fetchInvoicePayments } from "./api";

/** Read-only detail for one AR invoice: summary, lines, and payment history. */
export function InvoiceDetailPage() {
  const id = Number(useParams().id);

  const invoice = useQuery({
    queryKey: queryKeys.finance.invoice(id),
    queryFn: () => fetchInvoice(id),
  });
  const payments = useQuery({
    queryKey: queryKeys.finance.invoicePayments(id),
    queryFn: () => fetchInvoicePayments(id),
  });

  if (invoice.isPending) return <DashboardSkeleton />;
  if (invoice.isError) {
    return <ErrorState message="That invoice didn't load." onRetry={invoice.refetch} />;
  }
  const inv = invoice.data;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Link to="/finance/invoices" className="text-sm text-neutral-500 hover:text-neutral-900">
            ← Invoices
          </Link>
          <h1 className="text-xl font-semibold tracking-tight">Invoice #{inv.id}</h1>
          <StatusBadge status={inv.overdue ? "OVERDUE" : inv.status} />
        </div>
      </div>

      <DashboardCard title={inv.customerName}>
        <dl className="grid grid-cols-2 gap-x-8 gap-y-3 text-sm sm:grid-cols-3">
          <Field label="Issued" value={shortDate(inv.issueDate)} />
          <Field label="Due" value={shortDate(inv.dueDate)} />
          <Field label="Total" value={money(inv.total, inv.currency)} />
          <Field label="Paid" value={money(inv.amountPaid, inv.currency)} />
          <Field label="Outstanding" value={money(inv.outstanding, inv.currency)} emphasize />
        </dl>
      </DashboardCard>

      <DashboardCard title="Line items">
        <DataTable
          rows={inv.lines}
          rowKey={(l) => l.id}
          columns={[
            { header: "Description", cell: (l) => l.description },
            { header: "Qty", align: "right", cell: (l) => l.quantity },
            { header: "Unit price", align: "right", cell: (l) => money(l.unitPrice, inv.currency) },
            {
              header: "Line total",
              align: "right",
              cell: (l) => money(l.quantity * l.unitPrice, inv.currency),
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
