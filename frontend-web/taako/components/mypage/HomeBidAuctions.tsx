"use client";

import { useMyInfo } from "@/hooks/useMyInfo"

export default function HomeBidAuctions(){
    const {
      myBidAuctions,
      myBidLoading,
      myBidError,
    } = useMyInfo();
  
    if (myBidLoading) return <div>불러오는 중...</div>;
    if (myBidError) return <div>에러가 발생했습니다 😢</div>;

    console.log(myBidAuctions)
    return(
        <div>
        </div>
    )
}