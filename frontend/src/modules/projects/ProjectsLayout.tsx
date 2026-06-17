import { NavLink, Outlet } from "react-router-dom";

const TABS = [
  { label: "Dashboard", to: "/projects", end: true },
  { label: "Projects", to: "/projects/all", end: false },
];

/** Projects module shell: a sub-nav of Dashboard / Projects above the routed page. */
export function ProjectsLayout() {
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
                  ? "border-projects text-projects"
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
