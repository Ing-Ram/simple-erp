import type { ReactNode } from "react";

/** Column spec for {@link DataTable}: header, optional alignment, and a cell renderer. */
export interface Column<T> {
  header: string;
  align?: "left" | "right";
  cell: (row: T) => ReactNode;
}

/** Generic table. Numeric columns should be right-aligned and use tabular-nums in their cells. */
export function DataTable<T>({
  rows,
  rowKey,
  columns,
}: {
  rows: T[];
  rowKey: (row: T) => string | number;
  columns: Column<T>[];
}) {
  return (
    <table className="w-full text-sm">
      <thead>
        <tr className="border-b border-neutral-200 text-neutral-500">
          {columns.map((c, i) => (
            <th
              key={i}
              className={`pb-2 font-medium ${c.align === "right" ? "text-right" : "text-left"}`}
            >
              {c.header}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {rows.map((row) => (
          <tr key={rowKey(row)} className="border-b border-neutral-100 last:border-0">
            {columns.map((c, i) => (
              <td
                key={i}
                className={`py-2 ${c.align === "right" ? "text-right tabular-nums" : "text-left"}`}
              >
                {c.cell(row)}
              </td>
            ))}
          </tr>
        ))}
      </tbody>
    </table>
  );
}
