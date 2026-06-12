/**
 * Design tokens — the single source of visual identity. Components import these; they never
 * hardcode hex values. Keep the accents in sync with tailwind.config.js.
 */

/** Per-module accent colors, used for chart primaries and active navigation. */
export const moduleAccent = {
  finance: "#1E6F5C",
  hr: "#5B5BD6",
  sales: "#C2410C",
  projects: "#0E7490",
} as const;

export type ModuleKey = keyof typeof moduleAccent;

/** Semantic colors for values and status. */
export const semantic = {
  positive: "#15803D",
  negative: "#B91C1C",
  warning: "#B45309",
  neutral: "#475569",
} as const;

type Tone = "positive" | "negative" | "warning" | "neutral";

/**
 * One shared status → tone map used by StatusBadge everywhere, so a status is never restyled
 * per page. Covers AR/AP document statuses plus the derived OVERDUE marker.
 */
const statusTone: Record<string, Tone> = {
  DRAFT: "neutral",
  SENT: "neutral",
  PARTIALLY_PAID: "warning",
  PAID: "positive",
  APPROVED: "positive",
  VOID: "neutral",
  OVERDUE: "negative",
};

/** Resolves a status string to its tone, defaulting to neutral for unknown values. */
export function toneForStatus(status: string): Tone {
  return statusTone[status] ?? "neutral";
}

/** Tailwind utility classes for each tone — text plus a soft background. */
export const toneClasses: Record<Tone, string> = {
  positive: "bg-green-50 text-positive",
  negative: "bg-red-50 text-negative",
  warning: "bg-amber-50 text-warning",
  neutral: "bg-neutral-100 text-neutral-600",
};
