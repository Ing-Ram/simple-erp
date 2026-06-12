import { money, shortDate } from "../../../lib/format";
import { DashboardCard } from "../../../components/DashboardCard";
import { DataTable } from "../../../components/DataTable";
import { EmptyState } from "../../../components/EmptyState";
import type { PaymentRecord } from "../types";

/** Read-only payment history. Check payments show their routing and check numbers; others show "—". */
export function PaymentsHistory({ payments }: { payments: PaymentRecord[] }) {
  return (
    <DashboardCard title="Payment history">
      {payments.length === 0 ? (
        <EmptyState title="No payments yet" action="Recorded payments appear here." />
      ) : (
        <DataTable
          rows={payments}
          rowKey={(p) => p.id}
          columns={[
            { header: "Date", cell: (p) => shortDate(p.paymentDate) },
            { header: "Method", cell: (p) => p.method.replace(/_/g, " ") },
            { header: "Routing #", cell: (p) => p.routingNumber ?? "—" },
            { header: "Account #", cell: (p) => p.accountNumber ?? "—" },
            { header: "Check #", cell: (p) => p.checkNumber ?? "—" },
            { header: "Amount", align: "right", cell: (p) => money(p.amount, p.currency) },
          ]}
        />
      )}
    </DashboardCard>
  );
}
