import { useState } from "react";
import { money } from "../../../lib/format";
import type { PaymentMethod, PaymentRequest } from "../types";

const METHODS: PaymentMethod[] = ["BANK_TRANSFER", "CARD", "CHECK", "CASH"];

const todayIso = () => new Date().toISOString().slice(0, 10);

/** Form for recording a payment against an invoice or bill. */
export function PaymentForm({
  outstanding,
  currency,
  onSubmit,
  pending,
  error,
}: {
  outstanding: number;
  currency: string;
  onSubmit: (body: PaymentRequest) => void;
  pending: boolean;
  error: string | null;
}) {
  const [amount, setAmount] = useState(outstanding);
  const [paymentDate, setPaymentDate] = useState(todayIso());
  const [method, setMethod] = useState<PaymentMethod>("BANK_TRANSFER");
  const [routingNumber, setRoutingNumber] = useState("");
  const [accountNumber, setAccountNumber] = useState("");
  const [checkNumber, setCheckNumber] = useState("");
  const isCheck = method === "CHECK";

  return (
    <form
      className="space-y-4"
      onSubmit={(e) => {
        e.preventDefault();
        onSubmit({
          amount,
          currency,
          paymentDate,
          method,
          // Only send check details for CHECK payments, and only when filled in.
          routingNumber: isCheck && routingNumber ? routingNumber : undefined,
          accountNumber: isCheck && accountNumber ? accountNumber : undefined,
          checkNumber: isCheck && checkNumber ? checkNumber : undefined,
        });
      }}
    >
      <p className="text-sm text-neutral-500">
        Outstanding balance{" "}
        <span className="font-semibold tabular-nums text-neutral-900">{money(outstanding, currency)}</span>
      </p>

      <label className="block text-sm">
        <span className="text-neutral-600">Amount</span>
        <input
          type="number"
          min="0.01"
          step="0.01"
          max={outstanding}
          required
          value={amount}
          onChange={(e) => setAmount(Number(e.target.value))}
          className="mt-1 w-full rounded border border-neutral-300 px-2 py-1 text-right tabular-nums"
        />
      </label>

      <label className="block text-sm">
        <span className="text-neutral-600">Payment date</span>
        <input
          type="date"
          required
          value={paymentDate}
          onChange={(e) => setPaymentDate(e.target.value)}
          className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
        />
      </label>

      <label className="block text-sm">
        <span className="text-neutral-600">Method</span>
        <select
          value={method}
          onChange={(e) => setMethod(e.target.value as PaymentMethod)}
          className="mt-1 w-full rounded border border-neutral-300 px-2 py-1"
        >
          {METHODS.map((m) => (
            <option key={m} value={m}>
              {m.replace(/_/g, " ")}
            </option>
          ))}
        </select>
      </label>

      {isCheck && (
        <div className="space-y-3 rounded border border-neutral-200 bg-neutral-50 p-3">
          <div className="grid grid-cols-2 gap-3">
            <label className="block text-sm">
              <span className="text-neutral-600">Routing number</span>
              <input
                type="text"
                inputMode="numeric"
                placeholder="021000021"
                value={routingNumber}
                onChange={(e) => setRoutingNumber(e.target.value)}
                className="mt-1 w-full rounded border border-neutral-300 px-2 py-1 tabular-nums"
              />
            </label>
            <label className="block text-sm">
              <span className="text-neutral-600">Check number</span>
              <input
                type="text"
                inputMode="numeric"
                placeholder="1042"
                value={checkNumber}
                onChange={(e) => setCheckNumber(e.target.value)}
                className="mt-1 w-full rounded border border-neutral-300 px-2 py-1 tabular-nums"
              />
            </label>
          </div>
          <label className="block text-sm">
            <span className="text-neutral-600">Account number</span>
            <input
              type="text"
              inputMode="numeric"
              placeholder="000123456"
              value={accountNumber}
              onChange={(e) => setAccountNumber(e.target.value)}
              className="mt-1 w-full rounded border border-neutral-300 px-2 py-1 tabular-nums"
            />
            <span className="mt-1 block text-xs text-neutral-500">
              Stored masked — only the last 3 digits are kept.
            </span>
          </label>
        </div>
      )}

      {error && <p className="text-sm text-negative">{error}</p>}

      <button
        type="submit"
        disabled={pending}
        className="w-full rounded bg-finance px-3 py-2 text-sm font-medium text-white disabled:opacity-50"
      >
        {pending ? "Recording…" : "Record payment"}
      </button>
    </form>
  );
}
