// hooks/useBidAmount.ts
'use client';

import { useEffect, useMemo, useState } from 'react';

/** step(예: 0.01) 기준으로 반올림 */
function roundToStep(v: number, step: number) {
  if (!Number.isFinite(v) || !Number.isFinite(step) || step <= 0) return v;
  const decimals = (() => {
    const s = step.toString();
    if (s.includes('e-')) return parseInt(s.split('e-')[1], 10);
    const dot = s.indexOf('.');
    return dot >= 0 ? s.length - dot - 1 : 0;
  })();
  const q = Math.round(v / step) * step;
  return Number(q.toFixed(decimals));
}

export function useBidAmount(currentPrice: number, minIncrement: number, maxBid?: number) {
  const initial = useMemo(
    () => Math.max(currentPrice + minIncrement, 0),
    [currentPrice, minIncrement]
  );
  const [amount, setAmount] = useState<number>(initial);

  // currentPrice가 바뀌면 제안가 재조정
  useEffect(() => {
    setAmount(Math.max(currentPrice + minIncrement, 0));
  }, [currentPrice, minIncrement]);

  const minAllowed = currentPrice + minIncrement;

  const bump = (delta: number) => {
    setAmount((prev) => {
      const base = minAllowed;
      const start = Number.isFinite(prev) ? prev : base;
      const next = Math.max(start + delta, base);
      const capped = maxBid != null ? Math.min(next, maxBid) : next;
      return roundToStep(capped, minIncrement);
    });
  };

  /** 제출 가능 여부를 외부 상태와 함께 판단 */
  const canSubmit = (opts: { submitting: boolean; disabled?: boolean; iAmTop?: boolean }) => {
    if (opts.disabled || opts.submitting || opts.iAmTop) return false;
    if (!Number.isFinite(amount)) return false;
    if (amount < minAllowed) return false;
    if (maxBid != null && amount > maxBid) return false;
    return true;
  };

  return { amount, setAmount, bump, minAllowed, canSubmit };
}
