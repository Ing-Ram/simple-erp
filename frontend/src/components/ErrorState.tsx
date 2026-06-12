/** Failure state with a retry action. Copy says what went wrong and what to do next. */
export function ErrorState({ message, onRetry }: { message: string; onRetry: () => void }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 rounded-lg border border-neutral-200 bg-white p-10 text-center">
      <p className="text-sm text-neutral-600">{message}</p>
      <button
        onClick={onRetry}
        className="rounded border border-neutral-300 px-3 py-1.5 text-sm font-medium text-neutral-700 hover:bg-neutral-50"
      >
        Try again
      </button>
    </div>
  );
}
