import { useQuery } from "@tanstack/react-query";
import { getInfo, getMyBidAuction, getMySellAutcion } from "@/lib/mypage";
import { MyInfo, MyBidAuctions, MySellAuctions } from "@/types/auth";

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

  // isEnd 기준으로 배열 분리
  const ongoingAuctions = myBidAuctions.filter(auction => !auction.isEnd);
  const endedAuctions = myBidAuctions.filter(auction => auction.isEnd);
  const countOngoing = ongoingAuctions.length;
  const countEnded = endedAuctions.length;


  // 내 판매 경매 조회
  const {
    data: mySellAuctionsData, isLoading: mySellLoading, error: mySellError,
  } = useQuery<{ result: { content: MySellAuctions[] } }>({
    queryKey: ["mySellAuctions"],
    queryFn: getMySellAutcion,
  });
  const mySellAuctions = mySellAuctionsData?.result?.content ?? [];

  const ongoingSellAuctions = mySellAuctions.filter(auction => !auction.isEnd);
  const endedSellAuctions = mySellAuctions.filter(auction => auction.isEnd);

  return {
    myInfo: myInfo?.result ?? null, myInfoLoading, myInfoError,
    ongoingAuctions, countOngoing, endedAuctions, countEnded,
    myBidAuctions, myBidLoading, myBidError,
    mySellAuctionsData, ongoingSellAuctions, endedSellAuctions
  };
}
