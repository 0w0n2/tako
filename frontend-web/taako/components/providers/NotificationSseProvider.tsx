"use client";

import { useEffect, useRef } from "react";
import { useAuthStore } from "@/stores/useAuthStore";
import { useNotificationToastStore } from "@/stores/useNotificationToastStore";
import { useQueryClient } from "@tanstack/react-query";
import { resolveNotificationPath } from "@/lib/notificationRoute";
import { sseLog } from "@/lib/logger";

function isRecord(v: unknown): v is Record<string, any> {
	return !!v && typeof v === "object" && !Array.isArray(v);
}

function parseData(data: any): unknown {
	try {
		if (typeof data === "string") {
			return JSON.parse(data);
		}
	} catch {
		// not JSON string
	}
	return data;
}

function toToastFromEvent(eventName: string, payload: unknown) {
	// payload can be NotificationEvent-like or arbitrary map
	if (isRecord(payload)) {
		const title = payload.title ?? undefined;
		const message = payload.message ?? payload.msg ?? payload.text ?? undefined;
		const link = payload.link ?? payload.url ?? undefined;
		return { type: String(eventName), title, message: message ?? JSON.stringify(payload), link };
	}
	let msg = "";
	if (payload != null) {
		if (typeof payload === "string") msg = payload;
		else msg = JSON.stringify(payload);
	}
	return { type: String(eventName), title: undefined, message: msg, link: undefined };
}

export default function NotificationSseProvider() {
	const token = useAuthStore((s) => s.token);
	const pushToast = useNotificationToastStore((s) => s.push);
	const esRef = useRef<EventSource | null>(null);
	const reconnectTimer = useRef<any>(null);
	const qc = useQueryClient();

	useEffect(() => {
		const base = process.env.NEXT_PUBLIC_API_BASE_URL;
		if (!base) return;

		function cleanup() {
			if (reconnectTimer.current) {
				clearTimeout(reconnectTimer.current);
				reconnectTimer.current = null;
			}
			if (esRef.current) {
				esRef.current.close();
				esRef.current = null;
			}
		}

		if (!token) {
			cleanup();
			return;
		}

		const raw = token.startsWith("Bearer ") ? token.slice(7) : token;
		const url = `${base.replace(/\/$/, "")}/v1/notifications/stream?access_token=${encodeURIComponent(raw)}`;

		const connect = () => {
			cleanup();
			let es: EventSource | null = null;
			es = new EventSource(url, { withCredentials: true } as EventSourceInit);
			esRef.current = es;

			const log = (...args: any[]) => sseLog("notification", ...args);

			es.onopen = () => log("open");

			// attach event handlers
			const detach = attachHandlers(
				es,
				(name, data) => {
					// Known system events invalidate unread count
					const DEFAULT_TITLES: Record<string, string> = {
						WISH_AUCTION_STARTED: "위시한 경매가 시작됨",
						WISH_AUCTION_DUE_SOON: "위시한 경매 마감 임박",
						WISH_AUCTION_ENDED: "위시한 경매 종료",
						WISH_CARD_LISTED: "위시한 카드가 새로 등록됨",
						AUCTION_NEW_INQUIRY: "내 경매에 새 문의가 등록됨",
						INQUIRY_ANSWERED: "내 문의에 답변이 달렸습니다",
						AUCTION_WON: "경매 낙찰",
						BID_ACCEPTED: "입찰 반영 성공",
						BID_REJECTED: "입찰 거절",
						BID_FAILED: "입찰 반영 실패",
						BID_OUTBID: "상위 입찰 발생으로 최고가 지위 상실",
						DELIVERY_STARTED: "배송 시작",
						DELIVERY_STATUS_CHANGED: "배송 상태 변경",
						DELIVERY_CONFIRM_REQUEST: "구매 확정 요청",
						DELIVERY_CONFIRMED_SELLER: "구매 확정 완료(판매자)",
						AUCTION_CLOSED_SELLER: "경매 종료(판매자)",
						AUCTION_CANCELED: "경매 취소",
						NOTICE_NEW: "새 공지",
					} as any;

					const UI_LEVEL: Record<string, "info" | "success" | "warning" | "error"> = {
						WISH_AUCTION_STARTED: "info",
						WISH_AUCTION_DUE_SOON: "warning",
						WISH_AUCTION_ENDED: "info",
						WISH_CARD_LISTED: "info",
						AUCTION_NEW_INQUIRY: "info",
						INQUIRY_ANSWERED: "info",
						AUCTION_WON: "success",
						BID_ACCEPTED: "success",
						BID_REJECTED: "warning",
						BID_FAILED: "error",
						BID_OUTBID: "warning",
						DELIVERY_STARTED: "info",
						DELIVERY_STATUS_CHANGED: "info",
						DELIVERY_CONFIRM_REQUEST: "warning",
						DELIVERY_CONFIRMED_SELLER: "success",
						AUCTION_CLOSED_SELLER: "info",
						AUCTION_CANCELED: "warning",
						NOTICE_NEW: "info",
					};

					if (isRecord(data)) {
						const title = (data.title as string | undefined) ?? DEFAULT_TITLES[name];
						const message = (data.message as string | undefined) ?? (data.msg as string | undefined) ?? (data.text as string | undefined);
						const link = (data.link as string | undefined) ?? (data.url as string | undefined);
						const causeId = data.causeId as number | string | undefined;
						const level = UI_LEVEL[name] ?? "info";
						const computedPath = link || resolveNotificationPath(name, causeId as any) || undefined;
						pushToast({ type: level, rawType: name, title, message: message ?? JSON.stringify(data), link: computedPath, causeId });
					} else {
						const level = UI_LEVEL[name] ?? "info";
						const message = typeof data === "string" ? data : JSON.stringify(data);
						pushToast({ type: level, rawType: name, title: DEFAULT_TITLES[name], message });
					}

					// invalidate unread count for logical notifications
					if (name === "notification" || name === "notice" || name === "test" || name === "message" || name === "NOTICE_NEW") {
						qc.invalidateQueries({ queryKey: ["unreadCount"] });
					}
				},
				log
			);

			es.onerror = (_e) => {
				log("error, will reconnect");
				cleanup();
				reconnectTimer.current = setTimeout(connect, 3000);
			};

			return () => {
				detach();
				es?.close();
			};
		};

		const dispose = connect();
		return () => {
			dispose?.();
			cleanup();
		};
	}, [token]);

	return null;
}

function attachHandlers(es: EventSource, onData: (eventName: string, data: unknown) => void, log?: (...args: any[]) => void) {
	es.addEventListener("connected", (ev) => log?.("connected", ev));
	es.addEventListener("heartbeat", (ev) => log?.("heartbeat", ev));

	const eventNames = [
		"notification",
		"test",
		"info",
		"success",
		"warning",
		"error",
		"notice",
		"inquiry",
		"auction",
		"bid",
		"delivery",
		"wish",
		// domain specific types
		"WISH_AUCTION_STARTED",
		"WISH_AUCTION_DUE_SOON",
		"WISH_AUCTION_ENDED",
		"WISH_CARD_LISTED",
		"AUCTION_NEW_INQUIRY",
		"INQUIRY_ANSWERED",
		"AUCTION_WON",
		"BID_ACCEPTED",
		"BID_REJECTED",
		"BID_FAILED",
		"BID_OUTBID",
		"DELIVERY_STARTED",
		"DELIVERY_STATUS_CHANGED",
		"DELIVERY_CONFIRM_REQUEST",
		"DELIVERY_CONFIRMED_SELLER",
		"AUCTION_CLOSED_SELLER",
		"AUCTION_CANCELED",
		"NOTICE_NEW",
	];

	const handlers = eventNames.map((name) => {
		const fn = (ev: MessageEvent) => {
			const data = parseData((ev as any).data ?? null);
			// debug log for each named event
			log?.("event", name, data);
			onData(name, data);
		};
		es.addEventListener(name, fn as any);
		return { name, fn };
	});

	es.onmessage = (ev) => {
		const data = parseData((ev as any).data ?? null);
		// debug log for unnamed messages
		log?.("message", data);
		onData("message", data);
	};

	return () => {
		handlers.forEach(({ name, fn }) => es.removeEventListener(name, fn as any));
	};
}
