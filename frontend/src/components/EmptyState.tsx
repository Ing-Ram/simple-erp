/** Empty state whose copy invites the action that creates data. */
export function EmptyState({ title, action }: { title: string; action: string }) {
  return (
    <div className="flex flex-col items-center justify-center gap-1 py-10 text-center">
      <p className="text-sm font-medium text-neutral-700">{title}</p>
      <p className="text-sm text-neutral-500">{action}</p>
    </div>
  );
}
