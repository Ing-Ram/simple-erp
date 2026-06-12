import { money } from "../../../lib/format";
import type { LineItemRequest } from "../types";

/** Editable list of document lines (description, quantity, unit price). Currency is USD in v1. */
export function LineItemsEditor({
  lines,
  onChange,
}: {
  lines: LineItemRequest[];
  onChange: (lines: LineItemRequest[]) => void;
}) {
  const update = (i: number, patch: Partial<LineItemRequest>) =>
    onChange(lines.map((l, idx) => (idx === i ? { ...l, ...patch } : l)));

  const addLine = () =>
    onChange([...lines, { description: "", quantity: 1, unitPrice: 0, currency: "USD" }]);

  const removeLine = (i: number) => onChange(lines.filter((_, idx) => idx !== i));

  return (
    <div className="space-y-2">
      <div className="grid grid-cols-12 gap-2 text-xs font-medium text-neutral-500">
        <span className="col-span-6">Description</span>
        <span className="col-span-2 text-right">Qty</span>
        <span className="col-span-3 text-right">Unit price</span>
        <span className="col-span-1" />
      </div>
      {lines.map((line, i) => (
        <div key={i} className="grid grid-cols-12 items-center gap-2">
          <input
            className="col-span-6 rounded border border-neutral-300 px-2 py-1 text-sm"
            placeholder="Consulting"
            value={line.description}
            onChange={(e) => update(i, { description: e.target.value })}
          />
          <input
            type="number"
            min="0"
            step="0.01"
            className="col-span-2 rounded border border-neutral-300 px-2 py-1 text-right text-sm tabular-nums"
            value={line.quantity}
            onChange={(e) => update(i, { quantity: Number(e.target.value) })}
          />
          <input
            type="number"
            min="0"
            step="0.01"
            className="col-span-3 rounded border border-neutral-300 px-2 py-1 text-right text-sm tabular-nums"
            value={line.unitPrice}
            onChange={(e) => update(i, { unitPrice: Number(e.target.value) })}
          />
          <button
            type="button"
            onClick={() => removeLine(i)}
            className="col-span-1 text-neutral-400 hover:text-negative disabled:opacity-30"
            disabled={lines.length === 1}
            aria-label="Remove line"
          >
            ✕
          </button>
        </div>
      ))}
      <div className="flex items-center justify-between pt-1">
        <button
          type="button"
          onClick={addLine}
          className="text-sm font-medium text-finance hover:underline"
        >
          + Add line
        </button>
        <span className="text-sm text-neutral-500">
          Total{" "}
          <span className="font-semibold tabular-nums text-neutral-900">
            {money(lines.reduce((sum, l) => sum + l.quantity * l.unitPrice, 0))}
          </span>
        </span>
      </div>
    </div>
  );
}
