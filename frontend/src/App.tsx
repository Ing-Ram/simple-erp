import { NavLink, Navigate, Route, Routes } from "react-router-dom";
import { moduleAccent, type ModuleKey } from "./lib/tokens";
import { FinanceLayout } from "./modules/finance/FinanceLayout";
import { FinanceDashboard } from "./modules/finance/Dashboard";
import { InvoicesPage } from "./modules/finance/InvoicesPage";
import { InvoiceDetailPage } from "./modules/finance/InvoiceDetailPage";
import { BillsPage } from "./modules/finance/BillsPage";
import { BillDetailPage } from "./modules/finance/BillDetailPage";
import { HrDashboard } from "./modules/hr/Dashboard";
import { ModulePlaceholder } from "./components/ModulePlaceholder";

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
          </Route>
          <Route path="/hr" element={<HrDashboard />} />
          <Route path="/sales" element={<ModulePlaceholder module="Sales" />} />
          <Route path="/projects" element={<ModulePlaceholder module="Projects" />} />
        </Routes>
      </main>
    </div>
  );
}
