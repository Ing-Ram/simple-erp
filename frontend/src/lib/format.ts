/** Shared formatters — raw numbers never reach JSX. */
const usd = new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" });

export function money(amount: number, currency = "USD"): string {
  return currency === "USD"
    ? usd.format(amount)
    : new Intl.NumberFormat("en-US", { style: "currency", currency }).format(amount);
}

export function shortDate(iso: string): string {
  return new Date(iso).toLocaleDateString("en-US", { month: "short", day: "numeric" });
}

export function percent(ratio: number): string {
  return `${(ratio * 100).toFixed(1)}%`;
}

export function count(n: number): string {
  return new Intl.NumberFormat("en-US").format(n);
}

export function clockTime(iso: string): string {
  return new Date(iso).toLocaleTimeString("en-US", { hour: "numeric", minute: "2-digit" });
}
