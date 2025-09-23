import { useQuery } from "@tanstack/react-query";
import { getInfo, getMyBidAuction } from "@/lib/mypage";
import { MyInfo, MyBidAuctions } from "@/types/auth";

export function useMyInfo() {
  // 내 프로필 조회
  const {
    data: myInfo, isLoading: myInfoLoading, error: myInfoError,
  } = useQuery<{ result: MyInfo }>({
    queryKey: ["myInfo"],
    queryFn: getInfo,
  });

  // 내 입찰 경매 조회
  const {
    data: myBidAuctionsData, isLoading: myBidLoading, error: myBidError,
  } = useQuery<{ result: { content: MyBidAuctions[] } }>({
    queryKey: ["myBidAuctions"],
    queryFn: getMyBidAuction,
  });
  // 안전하게 배열 반환
  const myBidAuctions = myBidAuctionsData?.result?.content ?? [];

  return {
    myInfo: myInfo?.result ?? null,
    myInfoLoading,
    myInfoError,
    myBidAuctions,
    myBidLoading,
    myBidError,
  };
}
