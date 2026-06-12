import { toneClasses, toneForStatus } from "../lib/tokens";

/** Status pill colored from the one shared status→tone map — never restyle statuses per page. */
export function StatusBadge({ status }: { status: string }) {
  return (
    <span
      className={`inline-block rounded px-2 py-0.5 text-xs font-medium ${toneClasses[toneForStatus(status)]}`}
    >
      {status.replace(/_/g, " ")}
    </span>
  );
}
