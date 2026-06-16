import { useQuery } from "@tanstack/react-query";
import { queryKeys } from "../../lib/queryKeys";
import { count, money, percent } from "../../lib/format";
import { PageHeader } from "../../components/PageHeader";
import { DashboardCard } from "../../components/DashboardCard";
import { DataTable } from "../../components/DataTable";
import { ErrorState } from "../../components/ErrorState";
import { EmptyState } from "../../components/EmptyState";
import { DashboardSkeleton } from "../../components/DashboardSkeleton";
import { fetchReps } from "./api";

/** Salesperson leaderboard: deals won, value, win rate, and open pipeline per rep. */
export function RepsPage() {
  const { data, isPending, isError, refetch } = useQuery({
    queryKey: queryKeys.sales.reps,
    queryFn: fetchReps,
  });

  if (isPending) return <DashboardSkeleton />;
  if (isError) return <ErrorState message="Salesperson performance didn't load." onRetry={refetch} />;

  return (
    <div className="space-y-6">
      <PageHeader module="Salespeople" asOf={new Date()} />
      <DashboardCard title="Deal performance by salesperson">
        {data.length === 0 ? (
          <EmptyState title="No salespeople yet" action="Opportunities you create are owned by a salesperson and roll up here." />
        ) : (
          <DataTable
            rows={data}
            rowKey={(r) => r.employeeId}
            columns={[
              { header: "Salesperson", cell: (r) => r.name },
              { header: "Won", align: "right", cell: (r) => count(r.wonCount) },
              { header: "Won value", align: "right", cell: (r) => money(r.wonValue) },
              { header: "Avg deal", align: "right", cell: (r) => money(r.averageDealSize) },
              { header: "Lost", align: "right", cell: (r) => count(r.lostCount) },
              { header: "Win rate", align: "right", cell: (r) => percent(r.winRate) },
              { header: "Open pipeline", align: "right", cell: (r) => money(r.openPipelineWeighted) },
            ]}
          />
        )}
      </DashboardCard>
    </div>
  );
}
