'use client';

import { useEffect, useRef, useState, useCallback } from 'react';
import type { AuctionDetailProps } from '@/types/auction';
import { getAuctionDetail } from '@/lib/auction';
import { normalizeAxiosError, type NormalizedHttpError } from '@/lib/normalizeAxiosError';

export function useAuctionDetail(auctionId: number, historySize = 5) {
  const [data, setData] = useState<AuctionDetailProps | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<NormalizedHttpError | null>(null);

  const abortRef = useRef<AbortController | null>(null);
  const reqSeqRef = useRef(0); // 요청 시퀀스

  const fetchOnce = useCallback(async () => {
    if (!Number.isFinite(auctionId) || auctionId <= 0) {
        const err: NormalizedHttpError = {
            name: 'ValidationError',
            message: '유효하지 않은 경매 ID입니다.',
            safeMessage: '유효하지 않은 경매 ID입니다.',
            retryable: false,
        };
      setError(err);
      setLoading(false);
      return;
    }

    // 이전 요청 취소
    abortRef.current?.abort();
    const ctrl = new AbortController();
    abortRef.current = ctrl;

    const mySeq = ++reqSeqRef.current;
    setLoading(true);
    setError(null);

    try {
      const res = await getAuctionDetail(auctionId, { historySize, signal: ctrl.signal });

      // 마지막 요청만 반영
      if (mySeq === reqSeqRef.current) {
        setData(res);
      }
    } catch (e) {
      const ne = normalizeAxiosError(e);

      // 취소는 에러로 취급하지 않음
      if (ne.canceled) {
        return;
      }

      // 마지막 요청만 에러 반영
      if (mySeq === reqSeqRef.current) {
        console.error('[useAuctionDetail] error:', ne);
        setError(ne);
      }
    } finally {
      // 마지막 요청만 로딩 종료 반영
      if (mySeq === reqSeqRef.current) {
        setLoading(false);
      }
    }
  }, [auctionId, historySize]);

  useEffect(() => {
    fetchOnce();
    return () => {
      abortRef.current?.abort();
    };
  }, [fetchOnce]);

  const refetch = useCallback(() => {
    fetchOnce();
  }, [fetchOnce]);

  return { data, loading, error, refetch };
}
