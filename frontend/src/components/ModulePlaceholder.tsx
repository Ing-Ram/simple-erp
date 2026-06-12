import { PageHeader } from "./PageHeader";
import { EmptyState } from "./EmptyState";

/** Stand-in for modules not yet built (HR, Sales, Projects) so navigation is complete. */
export function ModulePlaceholder({ module }: { module: string }) {
  return (
    <div className="space-y-6">
      <PageHeader module={module} asOf={new Date()} />
      <div className="rounded-lg border border-neutral-200 bg-white">
        <EmptyState
          title={`${module} isn't built yet`}
          action="Finance is the first module. This one follows the same dashboard pattern."
        />
      </div>
    </div>
  );
}
