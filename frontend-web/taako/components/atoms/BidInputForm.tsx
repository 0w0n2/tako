// components/atoms/Input/BidInputForm.tsx
"use client";

import { useState } from "react";
import { queueBid } from "@/lib/auction";
import type { BidQueueResponse } from "@/types/bid";
import { getOrCreateUserUuid } from "@/lib/auth/uuid";
import { Button } from "@/components/ui/button";
import { useBidLock } from "@/hooks/useBidLock";
import { useBidAmount } from "@/hooks/useBidAmount";

type BidInputFormProps = {
  auctionId: string | number;
  currentPrice: number;
  minIncrement?: number;   // 기본 0.01
  maxBid?: number;
  token?: string;
  onBidApplied?: (nextPrice: number, resp: BidQueueResponse) => void;
  placeholder?: string;
  disabled?: boolean;
  className?: string;
};

export default function BidInputForm({
  auctionId,
  currentPrice,
  minIncrement = 0.01,
  maxBid,
  token,
  onBidApplied,
  placeholder,
  disabled,
  className,
}: BidInputFormProps) {
  // 상태 & 유효성: 금액
  const { amount, setAmount, bump, minAllowed, canSubmit } =
    useBidAmount(currentPrice, minIncrement, maxBid);

  // 상태: 내가 마지막 입찰자인지
  const { iAmTop, lockAsTop } = useBidLock(currentPrice);

  // 전송 상태/메시지
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string>("");
  const [okMsg, setOkMsg] = useState<string>("");

  const submitEnabled = canSubmit({ submitting, disabled, iAmTop });

  const handleSubmit = async (e?: React.FormEvent) => {
    e?.preventDefault();
    if (!submitEnabled) return;

    setError(""); setOkMsg(""); setSubmitting(true);

    try {
      // 사용자 UUID를 requestId로 사용(연속입찰 방지 키)
      const requestId = getOrCreateUserUuid();
      const res = await queueBid(auctionId, amount, { token, requestId });

      const serverPrice = res.result?.currentPrice ?? amount;

      // 부모에 최신가 반영
      onBidApplied?.(serverPrice, res);

      // 내가 마지막 입찰자 → 잠금
      lockAsTop(serverPrice);

      setOkMsg(`입찰 성공! 현재가: ${serverPrice.toLocaleString()} (상태: ${res.result?.status || "OK"})`);
    } catch (err: any) {
      setError(err?.message || "입찰 중 오류가 발생했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className={className ?? "flex flex-col gap-3 p-4 border rounded-xl bg-[#111] text-white"}>
      <label className="text-sm opacity-80">입찰가</label>
      <div className="flex gap-2 items-center">
        <input
          type="number"
          inputMode="decimal"
          min={minAllowed}
          step={minIncrement}
          value={Number.isFinite(amount) ? amount : ""}
          onChange={(e) => setAmount(Number(e.target.value || 0))}
          disabled={disabled || submitting || iAmTop}
          className="flex-1 px-3 py-2 rounded-md bg-[#1a1a1a] border border-[#333] focus:outline-none"
          placeholder={placeholder ?? `${minAllowed.toLocaleString()} 이상`}
        />
        {Button ? (
          <Button type="submit" disabled={!submitEnabled} className="min-w-24">
            {submitting ? "전송 중..." : iAmTop ? "상대 입찰 대기" : "입찰하기"}
          </Button>
        ) : (
          <button type="submit" disabled={!submitEnabled} className="px-4 py-2 rounded-md bg-blue-600 disabled:opacity-50">
            {submitting ? "전송 중..." : iAmTop ? "상대 입찰 대기" : "입찰하기"}
          </button>
        )}
      </div>

      <div className="flex gap-2">
        <button
          type="button"
          onClick={() => bump(minIncrement)}
          disabled={disabled || submitting || iAmTop}
          className="px-3 py-1 rounded-md bg-[#1f1f2a] border border-[#333]"
        >
          +{minIncrement.toLocaleString()}
        </button>
        <button
          type="button"
          onClick={() => bump(minIncrement * 5)}
          disabled={disabled || submitting || iAmTop}
          className="px-3 py-1 rounded-md bg-[#1f1f2a] border border-[#333]"
        >
          +{(minIncrement * 5).toLocaleString()}
        </button>
        <button
          type="button"
          onClick={() => setAmount(minAllowed)}
          disabled={disabled || submitting || iAmTop}
          className="px-3 py-1 rounded-md bg-[#1f1f2a] border border-[#333]"
        >
          최소입찰
        </button>
        {maxBid != null && (
          <span className="ml-auto text-xs opacity-60 self-center">
            최대 {maxBid.toLocaleString()}
          </span>
        )}
      </div>

      {!iAmTop && !submitting && Number.isFinite(amount) && amount < minAllowed && (
        <p className="text-xs text-red-400">
          입찰가는 {minAllowed.toLocaleString()} 이상이어야 합니다.
        </p>
      )}
      {iAmTop && (
        <p className="text-xs text-amber-300">내 입찰이 최상단입니다. 다른 사용자가 올릴 때까지 대기해 주세요.</p>
      )}
      {error && <p className="text-sm text-red-400">{error}</p>}
      {okMsg && <p className="text-sm text-emerald-400">{okMsg}</p>}
    </form>
  );
}
