// hooks/useAuctionPrice.ts
'use client';

import api from "@/lib/api"
import { useEffect, useRef, useState } from 'react';

type Options = {
  sseUrl?: string;        // 기본: /v1/auctions/{id}/live
  pollUrl?: string;       // 기본: /v1/auctions/{id}
  getPrice?: (json: any) => number | undefined; // 응답에서 currentPrice 추출기
  withCredentials?: boolean; // 세션 쿠키 사용 시 true
  pollMs?: number;            // 폴링 주기 (기본 4000ms)
  token?: string;             // 토큰을 querystring으로 넘기고 싶을 때 (?token=)
};

export function useAuctionPrice(
  auctionId: number | string,
  initialPrice: number,
  opts: Options = {}
) {
  const [price, setPrice] = useState<number>(initialPrice);
  const esRef = useRef<EventSource | null>(null);
  const pollTimerRef = useRef<any>(null);
  const backoffRef = useRef<number>(1500);

  useEffect(() => setPrice(initialPrice), [initialPrice]);

  useEffect(() => {
    const sseUrl =
      opts.sseUrl ??
      `${process.env.NEXT_PUBLIC_API_BASE_URL}/v1/auctions/${auctionId}/live${opts.token ? `?token=${encodeURIComponent(opts.token)}` : ''}`;
    const pollUrl = opts.pollUrl ?? `/v1/auctions/${auctionId}`;
    const pollMs = opts.pollMs ?? 4000;
    const getPrice =
      opts.getPrice ??
      ((j: any) => (typeof j?.result?.currentPrice === 'number' ? j.result.currentPrice : j?.currentPrice));

    const startPolling = () => {
      if (pollTimerRef.current) return;
      pollTimerRef.current = setInterval(async () => {
        try {
          const { data: j } = await api.get(pollUrl, {
            withCredentials: opts.withCredentials ?? true,
          });
          const next = getPrice(j);
          if (typeof next === 'number') setPrice((p) => (p === next ? p : next));
        } catch {
          // 폴링 에러는 무시 (다음 틱에 재시도)
        }
      }, pollMs);
    };

    const stopPolling = () => {
      if (pollTimerRef.current) {
        clearInterval(pollTimerRef.current);
        pollTimerRef.current = null;
      }
    };

    const startSSE = () => {
      try {
        // withCredentials: 쿠키 세션을 쓴다면 true
        const es = new EventSource(sseUrl, { withCredentials: opts.withCredentials ?? true } as EventSourceInit);
        esRef.current = es;

        const onMsg = (e: MessageEvent) => {
          try {
            const data = JSON.parse(e.data);
            const next =
              typeof data?.currentPrice === 'number'
                ? data.currentPrice
                : typeof data?.result?.currentPrice === 'number'
                ? data.result.currentPrice
                : undefined;
            if (typeof next === 'number') {
              setPrice((p) => (p === next ? p : next));
            }
          } catch {
            // invalid json → 무시
          }
        };

        es.addEventListener('message', onMsg); // 기본 이벤트
        es.addEventListener('price', onMsg);   // 커스텀 이벤트 명도 지원

        es.onerror = () => {
          // 연결 끊김: SSE 닫고 폴링으로 전환
          es.close();
          esRef.current = null;
          startPolling();

          // 지수 백오프 후 SSE 재연결 시도
          const delay = Math.min(backoffRef.current, 15000);
          setTimeout(() => {
            backoffRef.current = Math.min(backoffRef.current * 2, 15000);
            stopPolling();
            startSSE();
          }, delay);
        };
      } catch {
        // EventSource 자체 생성 실패 → 폴링으로
        startPolling();
      }
    };

    // 시작
    startSSE();

    // 정리
    return () => {
      esRef.current?.close();
      esRef.current = null;
      stopPolling();
      backoffRef.current = 1500;
    };
  }, [auctionId, opts.sseUrl, opts.pollUrl, opts.withCredentials, opts.pollMs, opts.token, opts.getPrice]);

  return [price, setPrice] as const;
}
