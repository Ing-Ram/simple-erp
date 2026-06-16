import { Link } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "../../lib/queryKeys";
import { money, shortDate } from "../../lib/format";
import { PageHeader } from "../../components/PageHeader";
import { DashboardCard } from "../../components/DashboardCard";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { ErrorState } from "../../components/ErrorState";
import { EmptyState } from "../../components/EmptyState";
import { DashboardSkeleton } from "../../components/DashboardSkeleton";
import { cancelOrder, fetchOrders, fulfillOrder, invoiceOrder } from "./api";
import type { SalesOrder } from "./types";

/** Sales orders: fulfil, invoice (hands off to Finance), cancel. */
export function OrdersPage() {
  const qc = useQueryClient();

  const { data, isPending, isError, refetch } = useQuery({
    queryKey: queryKeys.sales.orders,
    queryFn: fetchOrders,
  });

  /** Invoicing creates an AR invoice, so refresh Finance views too. */
  const invalidate = () => {
    qc.invalidateQueries({ queryKey: queryKeys.sales.orders });
    qc.invalidateQueries({ queryKey: queryKeys.sales.dashboard });
    qc.invalidateQueries({ queryKey: queryKeys.finance.dashboard });
    qc.invalidateQueries({ queryKey: queryKeys.finance.invoices });
  };

  const fulfill = useMutation({ mutationFn: fulfillOrder, onSuccess: invalidate });
  const invoice = useMutation({ mutationFn: invoiceOrder, onSuccess: invalidate });
  const cancel = useMutation({ mutationFn: cancelOrder, onSuccess: invalidate });

  if (isPending) return <DashboardSkeleton />;
  if (isError) return <ErrorState message="Orders didn't load." onRetry={refetch} />;

  return (
    <div className="space-y-6">
      <PageHeader module="Orders" asOf={new Date()} />

      <DashboardCard title="All orders">
        {data.length === 0 ? (
          <EmptyState title="No orders yet" action="Orders are created when you win an opportunity." />
        ) : (
          <DataTable
            rows={data}
            rowKey={(r) => r.id}
            columns={[
              { header: "#", cell: (r) => r.id },
              { header: "Customer", cell: (r) => r.customerName },
              { header: "Owner", cell: (r) => r.ownerName },
              { header: "Status", cell: (r) => <StatusBadge status={r.status} /> },
              { header: "Ordered", align: "right", cell: (r) => shortDate(r.orderDate) },
              { header: "Total", align: "right", cell: (r) => money(r.total, r.currency) },
              { header: "", align: "right", cell: (r) => <RowActions order={r} /> },
            ]}
          />
        )}
      </DashboardCard>
    </div>
  );

  function RowActions({ order }: { order: SalesOrder }) {
    return (
      <div className="flex items-center justify-end gap-3 text-sm font-medium">
        {order.status === "OPEN" && (
          <>
            <button className="text-sales hover:underline" onClick={() => fulfill.mutate(order.id)}>
              Fulfill
            </button>
            <button className="text-negative hover:underline" onClick={() => cancel.mutate(order.id)}>
              Cancel
            </button>
          </>
        )}
        {order.status === "FULFILLED" && (
          <button className="text-sales hover:underline" onClick={() => invoice.mutate(order.id)}>
            Invoice
          </button>
        )}
        {order.status === "INVOICED" && order.invoiceId != null && (
          <Link to={`/finance/invoices/${order.invoiceId}`} className="text-finance hover:underline">
            Invoice #{order.invoiceId}
          </Link>
        )}
      </div>
    );
  }
}
