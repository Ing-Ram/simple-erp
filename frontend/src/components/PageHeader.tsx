import { shortDate } from "../lib/format";

/** Top of every dashboard: module name plus the as-of date the figures reflect. */
export function PageHeader({ module, asOf }: { module: string; asOf: Date }) {
  return (
    <header className="flex items-baseline justify-between border-b border-neutral-200 pb-4">
      <h1 className="text-xl font-semibold tracking-tight text-neutral-900">{module}</h1>
      <span className="text-sm text-neutral-500">As of {shortDate(asOf.toISOString())}</span>
    </header>
  );
}
