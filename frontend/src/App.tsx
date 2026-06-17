import { NavLink, Navigate, Route, Routes } from "react-router-dom";
import { moduleAccent, type ModuleKey } from "./lib/tokens";
import { FinanceLayout } from "./modules/finance/FinanceLayout";
import { FinanceDashboard } from "./modules/finance/Dashboard";
import { InvoicesPage } from "./modules/finance/InvoicesPage";
import { InvoiceDetailPage } from "./modules/finance/InvoiceDetailPage";
import { BillsPage } from "./modules/finance/BillsPage";
import { BillDetailPage } from "./modules/finance/BillDetailPage";
import { PartiesPage } from "./modules/finance/PartiesPage";
import { HrLayout } from "./modules/hr/HrLayout";
import { HrDashboard } from "./modules/hr/Dashboard";
import { EmployeesPage } from "./modules/hr/EmployeesPage";
import { DepartmentsPage } from "./modules/hr/DepartmentsPage";
import { LeaveRequestsPage } from "./modules/hr/LeaveRequestsPage";
import { RollCallPage } from "./modules/hr/RollCallPage";
import { SalesLayout } from "./modules/sales/SalesLayout";
import { SalesDashboard } from "./modules/sales/Dashboard";
import { LeadsPage } from "./modules/sales/LeadsPage";
import { OpportunitiesPage } from "./modules/sales/OpportunitiesPage";
import { OrdersPage } from "./modules/sales/OrdersPage";
import { RepsPage } from "./modules/sales/RepsPage";
import { ClosedDealsPage } from "./modules/sales/ClosedDealsPage";
import { ProjectsLayout } from "./modules/projects/ProjectsLayout";
import { ProjectsDashboard } from "./modules/projects/Dashboard";
import { ProjectsPage } from "./modules/projects/ProjectsPage";
import { ProjectDetailPage } from "./modules/projects/ProjectDetailPage";

const MODULES: { key: ModuleKey; label: string; path: string }[] = [
  { key: "finance", label: "Finance", path: "/finance" },
  { key: "hr", label: "HR", path: "/hr" },
  { key: "sales", label: "Sales", path: "/sales" },
  { key: "projects", label: "Projects", path: "/projects" },
];

/** Left-rail navigation; the active module is underlined in its accent color. */
function Nav() {
  return (
    <nav className="flex flex-col gap-1 p-4">
      <div className="mb-4 px-2 text-sm font-semibold tracking-tight text-neutral-900">SimpleERP</div>
      {MODULES.map((m) => (
        <NavLink
          key={m.key}
          to={m.path}
          className={({ isActive }) =>
            `rounded px-2 py-1.5 text-sm font-medium ${
              isActive ? "bg-neutral-100 text-neutral-900" : "text-neutral-500 hover:text-neutral-900"
            }`
          }
          style={({ isActive }) =>
            isActive ? { boxShadow: `inset 2px 0 0 ${moduleAccent[m.key]}` } : undefined
          }
        >
          {m.label}
        </NavLink>
      ))}
    </nav>
  );
}

/** App shell: persistent module nav beside the routed dashboard. */
export function App() {
  return (
    <div className="flex min-h-screen">
      <aside className="w-48 shrink-0 border-r border-neutral-200 bg-white">
        <Nav />
      </aside>
      <main className="flex-1 p-8">
        <Routes>
          <Route path="/" element={<Navigate to="/finance" replace />} />
          <Route path="/finance" element={<FinanceLayout />}>
            <Route index element={<FinanceDashboard />} />
            <Route path="invoices" element={<InvoicesPage />} />
            <Route path="invoices/:id" element={<InvoiceDetailPage />} />
            <Route path="bills" element={<BillsPage />} />
            <Route path="bills/:id" element={<BillDetailPage />} />
            <Route path="customers" element={<PartiesPage kind="customers" />} />
            <Route path="vendors" element={<PartiesPage kind="vendors" />} />
          </Route>
          <Route path="/hr" element={<HrLayout />}>
            <Route index element={<HrDashboard />} />
            <Route path="employees" element={<EmployeesPage />} />
            <Route path="departments" element={<DepartmentsPage />} />
            <Route path="leave" element={<LeaveRequestsPage />} />
            <Route path="roll-call" element={<RollCallPage />} />
          </Route>
          <Route path="/sales" element={<SalesLayout />}>
            <Route index element={<SalesDashboard />} />
            <Route path="leads" element={<LeadsPage />} />
            <Route path="opportunities" element={<OpportunitiesPage />} />
            <Route path="orders" element={<OrdersPage />} />
            <Route path="reps" element={<RepsPage />} />
            <Route path="closed" element={<ClosedDealsPage />} />
          </Route>
          <Route path="/projects" element={<ProjectsLayout />}>
            <Route index element={<ProjectsDashboard />} />
            <Route path="all" element={<ProjectsPage />} />
            <Route path="all/:id" element={<ProjectDetailPage />} />
          </Route>
          {/* Unknown URLs fall back to the default module rather than a blank screen. */}
          <Route path="*" element={<Navigate to="/finance" replace />} />
        </Routes>
      </main>
    </div>
  );
}
