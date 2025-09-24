// hooks/useMyInfo.ts
import { useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import { getInfo, getMyBidAuction } from "@/lib/mypage";
import type { MyInfo, MyBidAuctions } from "@/types/auth";

type Page<T> = {
  content: T[];
  // 필요한 경우: totalElements?: number; totalPages?: number; number?: number; size?: number;
};

export function useMyInfo() {
  const { data: myInfo, isLoading: myInfoLoading, error: myInfoError } =
    useQuery<MyInfo, Error>({ queryKey: ["myInfo"], queryFn: getInfo });

  // 진행 중
  const { data: ongoingPage, isLoading: ongoingLoading, error: ongoingError } =
    useQuery<Page<MyBidAuctions>, Error>({
      queryKey: ["myBidAuctions", { ended: false, page: 0, size: 20 }],
      queryFn: () => getMyBidAuction({ ended: false, page: 0, size: 20 }),
    });

  // 종료
  const { data: endedPage, isLoading: endedLoading, error: endedError } =
    useQuery<Page<MyBidAuctions>, Error>({
      queryKey: ["myBidAuctions", { ended: true, page: 0, size: 20 }],
      queryFn: () => getMyBidAuction({ ended: true, page: 0, size: 20 }),
    });

  const ongoingAuctions = ongoingPage?.content ?? [];
  const endedAuctions   = endedPage?.content ?? [];

  // 사이드메뉴에서 쓰기 좋게 합치기 (원하는 정렬이 있으면 여기서 정렬)
  const myBidAuctions = useMemo(
    () => [...ongoingAuctions, ...endedAuctions],
    [ongoingAuctions, endedAuctions]
  );

  return {
    myInfo: myInfo ?? null,
    myInfoLoading,
    myInfoError,

    ongoingAuctions,
    countOngoing: ongoingAuctions.length,

    endedAuctions,
    countEnded: endedAuctions.length,

    // 새로 추가 ↓
    myBidAuctions,

    myBidLoading: ongoingLoading || endedLoading,
    myBidError: ongoingError || endedError,
  };
}
