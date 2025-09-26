"use client";

import { useState, useEffect, useRef } from "react";
import { queueBid } from "@/lib/auction";
import type { BidQueueResponse } from "@/types/bid";
import { getOrCreateUserUuid, onUuidReset } from "@/lib/auth/uuid";
import { Button } from "@/components/ui/button";
import { useBidLock } from "@/hooks/useBidLock";
import { useBidAmount } from "@/hooks/useBidAmount";

type BidInputFormProps = {
	auctionId: string | number;
	currentPrice: number;
	minIncrement: number;
	maxBid?: number;
	token?: string;
	onBidApplied?: (nextPrice: number, resp: BidQueueResponse) => void;
	placeholder?: string;
	disabled?: boolean;
	className?: string;
};

export default function BidInputForm({ auctionId, currentPrice, minIncrement, maxBid, token, onBidApplied, placeholder, disabled, className }: Readonly<BidInputFormProps>) {
	// 상태 & 유효성: 금액
	const { amount, setAmount, bump, minAllowed, canSubmit } = useBidAmount(currentPrice, minIncrement, maxBid);

	// 상태: 내가 마지막 입찰자인지
	const { iAmTop, lockAsTop, resetLock } = useBidLock(currentPrice);

	// iAmTop 이었다가 해제된 경우(다른 사용자가 더 높게 입찰) 기존 성공 메시지를 교체
	const wasTopRef = useRef(false);
	useEffect(() => {
		if (iAmTop) {
			wasTopRef.current = true;
			return;
		}
		// iAmTop -> false 로 전환된 순간
		if (wasTopRef.current) {
			// 내가 최고가였지만 이제 아님
			setOkMsg("다른 사용자가 더 높은 금액으로 입찰하여 현재 최고가가 아닙니다.");
			wasTopRef.current = false;
		}
	}, [iAmTop]);

	// UUID 리셋 시 잠금 해제 및 메시지 초기화
	useEffect(() => {
		const unsubscribe = onUuidReset(() => {
			resetLock();
			setError("");
			setOkMsg("");
		});
		return unsubscribe;
	}, [resetLock]);

	// 전송 상태/메시지
	const [submitting, setSubmitting] = useState(false);
	const [error, setError] = useState<string>("");
	const [okMsg, setOkMsg] = useState<string>("");
	// QUEUED 상태에서 나중에 SSE 로 반영되었는지 판별하기 위한 대기 금액
	const [queuedAmount, setQueuedAmount] = useState<number | null>(null);
	const prevPriceRef = useRef<number>(currentPrice);

	// currentPrice 변경 감시: QUEUED → (수용 or 선점 실패) 판정
	useEffect(() => {
		const prev = prevPriceRef.current;
		prevPriceRef.current = currentPrice;
		if (queuedAmount == null) return; // 대기 중 아님

		if (currentPrice === queuedAmount) {
			// 내가 큐에 넣은 금액이 현재가로 확정됨
			lockAsTop(currentPrice);
			setOkMsg(`입찰 성공! 현재가: ${currentPrice.toLocaleString()} (확정)`);
			setQueuedAmount(null);
			return;
		}
		// 다른 사용자가 더 높게 선점
		if (currentPrice > queuedAmount && currentPrice !== prev) {
			setOkMsg("대기 중인 입찰이 확정되기 전에 다른 사용자가 더 높은 금액으로 입찰했습니다.");
			setQueuedAmount(null);
		}
	}, [currentPrice, queuedAmount, lockAsTop]);

	const submitEnabled = canSubmit({ submitting, disabled, iAmTop });

	const handleSubmit = async (e?: React.FormEvent) => {
		e?.preventDefault();
		if (!submitEnabled) return;

		setError("");
		setOkMsg("");
		setSubmitting(true);

		try {
			// 사용자 UUID를 requestId로 사용(연속입찰 방지 키)
			const requestId = getOrCreateUserUuid();
			const res = await queueBid(auctionId, amount, { token, requestId });

			const serverPrice = res.result?.currentPrice ?? amount;

			// 부모에 최신가 반영
			onBidApplied?.(serverPrice, res);

			const status = (res.result?.status || "").toUpperCase();
			// ACCEPTED 인 경우에만 현재가 확정 & 락
			if (status === "ACCEPTED" || status === "SUCCESS") {
				lockAsTop(serverPrice);
				setOkMsg(`입찰 성공! 현재가: ${serverPrice.toLocaleString()} (확정)`);
			} else if (status === "QUEUED") {
				// 큐에 들어간 상태: 아직 다른 사용자/서버 승인 대기
				setOkMsg(`입찰 대기중... (큐 처리) 잠시 후 반영됩니다.`);
				setQueuedAmount(amount); // 나중에 currentPrice 변화로 판정할 기준 저장
			} else {
				setOkMsg(`입찰 처리: ${status || "OK"}`);
			}
		} catch (err: any) {
			setError(err?.message || "입찰 중 오류가 발생했습니다.");
		} finally {
			setSubmitting(false);
		}
	};

	let submitLabel: string;
	if (submitting) submitLabel = "전송 중...";
	else if (iAmTop) submitLabel = "상대 입찰 대기";
	else submitLabel = "입찰하기";

	return (
		<form onSubmit={handleSubmit} className={className ?? "flex flex-col gap-3 p-4 border rounded-xl bg-[#111] text-white"}>
			<label className="text-sm opacity-80" htmlFor={`bid-input-${auctionId}`}>
				입찰가
			</label>
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
					id={`bid-input-${auctionId}`}
				/>
				{Button ? (
					<Button type="submit" disabled={!submitEnabled} className="min-w-24">
						{submitLabel}
					</Button>
				) : (
					<button type="submit" disabled={!submitEnabled} className="px-4 py-2 rounded-md bg-blue-600 disabled:opacity-50">
						{submitLabel}
					</button>
				)}
			</div>

			<div className="flex gap-2">
				<button type="button" onClick={() => bump(minIncrement)} disabled={disabled || submitting || iAmTop} className="px-3 py-1 rounded-md bg-[#1f1f2a] border border-[#333]">
					+{minIncrement.toLocaleString()}
				</button>
				<button type="button" onClick={() => bump(minIncrement * 5)} disabled={disabled || submitting || iAmTop} className="px-3 py-1 rounded-md bg-[#1f1f2a] border border-[#333]">
					+{(minIncrement * 5).toLocaleString()}
				</button>
				<button type="button" onClick={() => setAmount(minAllowed)} disabled={disabled || submitting || iAmTop} className="px-3 py-1 rounded-md bg-[#1f1f2a] border border-[#333]">
					최소입찰
				</button>
				{maxBid != null && <span className="ml-auto text-xs opacity-60 self-center">최대 {maxBid.toLocaleString()}</span>}
			</div>

			{!iAmTop && !submitting && Number.isFinite(amount) && amount < minAllowed && <p className="text-xs text-red-400">입찰가는 {minAllowed.toLocaleString()} 이상이어야 합니다.</p>}
			{iAmTop && !error && <p className="text-xs text-amber-300">내 입찰이 현재 최고가입니다. 다른 사용자가 올릴 때까지 대기하세요.</p>}
			{error && <p className="text-sm text-red-400">{error}</p>}
			{okMsg && <p className="text-sm text-emerald-400">{okMsg}</p>}
		</form>
	);
}
