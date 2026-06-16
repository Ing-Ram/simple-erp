import { NavLink, Outlet } from "react-router-dom";

const TABS = [
  { label: "Dashboard", to: "/sales", end: true },
  { label: "Salespeople", to: "/sales/reps", end: false },
  { label: "Closed deals", to: "/sales/closed", end: false },
];

/** Sales module shell: a sub-nav of Dashboard / Salespeople / Closed deals above the routed page. */
export function SalesLayout() {
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
                  ? "border-sales text-sales"
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
