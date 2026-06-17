import { Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { queryKeys } from "../../lib/queryKeys";
import { count, money } from "../../lib/format";
import { moduleAccent, type ModuleKey } from "../../lib/tokens";
import { useAuth } from "../../lib/auth";
import { fetchFinanceDashboard } from "../finance/api";
import { fetchHrDashboard } from "../hr/api";
import { fetchSalesDashboard } from "../sales/api";
import { fetchProjectsDashboard } from "../projects/api";

/** One module's headline number, fetched from its dashboard endpoint. */
function ModuleCard({
  module,
  title,
  to,
  label,
  value,
  loading,
}: {
  module: ModuleKey;
  title: string;
  to: string;
  label: string;
  value: string | undefined;
  loading: boolean;
}) {
  return (
    <Link
      to={to}
      className="rounded-lg border border-neutral-200 bg-white p-5 transition hover:shadow-sm"
      style={{ borderTopWidth: 3, borderTopColor: moduleAccent[module] }}
    >
      <div className="text-sm font-medium text-neutral-700">{title}</div>
      <div className="mt-3 text-2xl font-semibold tabular-nums text-neutral-900">
        {loading ? "…" : value}
      </div>
      <div className="text-sm text-neutral-500">{label}</div>
    </Link>
  );
}

/** Landing page: a card per module linking in, each showing that module's top KPI. */
export function HomePage() {
  const { user } = useAuth();

  const finance = useQuery({ queryKey: queryKeys.finance.dashboard, queryFn: fetchFinanceDashboard });
  const hr = useQuery({ queryKey: queryKeys.hr.dashboard, queryFn: fetchHrDashboard });
  const sales = useQuery({ queryKey: queryKeys.sales.dashboard, queryFn: fetchSalesDashboard });
  const projects = useQuery({ queryKey: queryKeys.projects.dashboard, queryFn: fetchProjectsDashboard });

  return (
    <div className="space-y-6">
      <header className="border-b border-neutral-200 pb-4">
        <h1 className="text-xl font-semibold tracking-tight text-neutral-900">
          Welcome back, {user?.displayName ?? "there"}
        </h1>
        <p className="text-sm text-neutral-500">A snapshot across the business — open a module to dig in.</p>
      </header>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <ModuleCard
          module="finance"
          title="Finance"
          to="/finance"
          label="AR outstanding"
          loading={finance.isPending}
          value={finance.data && money(finance.data.arOutstanding)}
        />
        <ModuleCard
          module="hr"
          title="HR"
          to="/hr"
          label="Active headcount"
          loading={hr.isPending}
          value={hr.data && count(hr.data.activeHeadcount)}
        />
        <ModuleCard
          module="sales"
          title="Sales"
          to="/sales"
          label="Open pipeline (weighted)"
          loading={sales.isPending}
          value={sales.data && money(sales.data.openPipelineWeighted)}
        />
        <ModuleCard
          module="projects"
          title="Projects"
          to="/projects"
          label="Active projects"
          loading={projects.isPending}
          value={projects.data && count(projects.data.activeProjects)}
        />
      </div>
    </div>
  );
}
