/** One KPI tile. The value is a pre-formatted string from lib/format — never a raw number. */
export function StatCard({
  label,
  value,
  delta,
}: {
  label: string;
  value: string;
  delta?: { value: string; direction: "up" | "down" };
}) {
  return (
    <div className="rounded-lg border border-neutral-200 bg-white p-4">
      <div className="text-sm text-neutral-500">{label}</div>
      <div className="mt-1 text-2xl font-semibold tabular-nums text-neutral-900">{value}</div>
      {delta && (
        <div
          className={`mt-1 text-sm tabular-nums ${
            delta.direction === "up" ? "text-positive" : "text-negative"
          }`}
        >
          {delta.direction === "up" ? "▲" : "▼"} {delta.value}
        </div>
      )}
    </div>
  );
}
