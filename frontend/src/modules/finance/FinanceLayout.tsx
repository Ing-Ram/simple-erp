import { NavLink, Outlet } from "react-router-dom";

const TABS = [
  { label: "Dashboard", to: "/finance", end: true },
  { label: "Invoices", to: "/finance/invoices", end: false },
  { label: "Bills", to: "/finance/bills", end: false },
  { label: "Customers", to: "/finance/customers", end: false },
  { label: "Vendors", to: "/finance/vendors", end: false },
];

/** Finance module shell: a sub-nav of Dashboard / Invoices / Bills above the routed page. */
export function FinanceLayout() {
  return (
    <div className="space-y-6">
      <nav className="flex gap-4 border-b border-neutral-200">
        {TABS.map((t) => (
          <NavLink
            key={t.to}
            to={t.to}
            end={t.end}
            className={({ isActive }) =>
              `-mb-px border-b-2 px-1 pb-2 text-sm font-medium ${
                isActive
                  ? "border-finance text-finance"
                  : "border-transparent text-neutral-500 hover:text-neutral-900"
              }`
            }
          >
            {t.label}
          </NavLink>
        ))}
      </nav>
      <Outlet />
    </div>
  );
}
