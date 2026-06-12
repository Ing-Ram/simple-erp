/** Loading placeholder that mirrors the dashboard grid so layout doesn't jump when data lands. */
export function DashboardSkeleton() {
  return (
    <div className="animate-pulse space-y-6">
      <div className="h-8 w-40 rounded bg-neutral-200" />
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="h-24 rounded-lg bg-neutral-200" />
        ))}
      </div>
      <div className="grid gap-4 lg:grid-cols-3">
        <div className="h-72 rounded-lg bg-neutral-200 lg:col-span-2" />
        <div className="h-72 rounded-lg bg-neutral-200" />
      </div>
      <div className="h-48 rounded-lg bg-neutral-200" />
    </div>
  );
}
