import { useQuery } from "@tanstack/react-query";
import { getInfo, getMyBidAuction } from "@/lib/mypage";
import type { MyInfo } from "@/types/auth";

export function useMyInfo() {
  const { data: myInfo, isLoading: myInfoLoading, error: myInfoError } =
    useQuery<MyInfo>({ queryKey: ["myInfo"], queryFn: getInfo });

  // 진행 중 (ended=false)
  const { data: ongoingPage, isLoading: ongoingLoading, error: ongoingError } =
    useQuery({
      queryKey: ["myBidAuctions", { ended: false, page: 0, size: 20 }],
      queryFn: () => getMyBidAuction({ ended: false, page: 0, size: 20 }),
    });

  // 종료 (ended=true)
  const { data: endedPage, isLoading: endedLoading, error: endedError } =
    useQuery({
      queryKey: ["myBidAuctions", { ended: true, page: 0, size: 20 }],
      queryFn: () => getMyBidAuction({ ended: true, page: 0, size: 20 }),
    });

  const ongoingAuctions = ongoingPage?.content ?? [];
  const endedAuctions   = endedPage?.content ?? [];

  return {
    myInfo: myInfo ?? null,
    myInfoLoading, myInfoError,
    ongoingAuctions, countOngoing: ongoingAuctions.length,
    endedAuctions,   countEnded:   endedAuctions.length,
    myBidLoading: ongoingLoading || endedLoading,
    myBidError: ongoingError || endedError,
  };
}
