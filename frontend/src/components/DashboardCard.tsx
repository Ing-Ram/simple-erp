import type { ReactNode } from "react";

/** Section wrapper with a title and hairline border; the building block of the dashboard grid. */
export function DashboardCard({
  title,
  className,
  children,
}: {
  title: string;
  className?: string;
  children: ReactNode;
}) {
  return (
    <section className={`rounded-lg border border-neutral-200 bg-white p-4 ${className ?? ""}`}>
      <h2 className="mb-3 text-sm font-medium text-neutral-700">{title}</h2>
      {children}
    </section>
  );
}
