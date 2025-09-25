// components/nft/BuyEndedAuction.tsx (위치는 프로젝트 구조에 맞게)
"use client";

import Image from "next/image";
import { ChevronRight } from 'lucide-react';
import { useMyInfo } from "@/hooks/useMyInfo";
import ConfirmReceiptButton from "@/components/nft/ConfirmReceiptButton";
import { useDelivery } from "@/hooks/useDelivery";
import { useEscrowAddress } from "@/hooks/useEscrowAddress";
import { depositToEscrow } from "@/lib/bc/escrow";
import PaySection from '@/components/nft/PaySection'; // ← 경로 수정!
import type { MyBidAuctions } from "@/types/auth";

function DeliveryBadge({ auctionId }: { auctionId: number }) {
  const { info, status, hasTracking } = useDelivery(auctionId);

  const label = (() => {
    if (!hasTracking) return "운송장 대기";
    switch (status) {
      case "WAITING": return "배송 준비중";
      case "IN_PROGRESS": return "배송중";
      case "COMPLETED": return "배송완료";
      case "CONFIRMED": return "구매확정됨";
      default: return status;
    }
  })();

  return (
    <div className="px-6 py-2 bg-[#171725] border-b border-[#353535] flex items-center justify-between">
      <p className="text-xs text-[#a5a5a5]">
        배송상태: <span className="text-[#e1e1e1]">{label}</span>
        {info?.trackingNumber ? <span className="ml-2 text-[#8bb4ff]">#{info.trackingNumber}</span> : null}
      </p>
    </div>
  );
}

/** PaySection을 훅 규칙 지키며 경매별로 붙이는 래퍼 */
function PaySectionWrapper({ auctionId, priceEth }: { auctionId: number; priceEth: number }) {
  const { info } = useDelivery(auctionId);
  const trackingNumber = info?.trackingNumber ?? null;
  const { data: escrowAddress } = useEscrowAddress(auctionId);

  const onPay = async (defaultAddressId: number) => {
    if (!escrowAddress) {
      alert("에스크로 주소를 가져오지 못했습니다.");
      return;
    }
    try {
      const receipt = await depositToEscrow(escrowAddress, priceEth);
      console.log("Deposit tx receipt:", receipt, { auctionId, defaultAddressId });
      alert("결제가 완료되었습니다. 트랜잭션이 확인되었습니다.");
      // TODO: 결제 성공 후 서버 리포팅(API) 필요 시 호출
    } catch (e: any) {
      if (e?.code === 4001) {
        alert("사용자가 결제를 취소했습니다.");
      } else {
        alert(e?.message ?? "결제에 실패했습니다.");
      }
    }
  };

  return (
    <div className="px-6 pb-6">
      <PaySection auctionId={auctionId} trackingNumber={trackingNumber} onPay={onPay} />
    </div>
  );
}

export default function BuyEndedAuction(){
  const { endedAuctions, myBidLoading, myBidError } = useMyInfo();

  if (myBidLoading) return <div className="text-center text-[#a5a5a5] text-sm py-20">불러오는 중...</div>;
  if (myBidError) return <div className="text-center text-[#a5a5a5] text-sm py-20">에러가 발생했습니다 😢</div>;
  if (!endedAuctions || endedAuctions.length === 0) {
    return <div className="text-center text-[#a5a5a5] text-sm py-20">종료된 경매가 없습니다.</div>;
  }

  return (
    <div>
      {endedAuctions.map((item: MyBidAuctions) => {
        const auctionId = Number(item.auctionId);
        if (!Number.isFinite(auctionId)) {
          console.warn("유효하지 않은 auctionId", item);
          return null;
        }

        // NOTE: 백엔드 단위와 ETH 단위가 다르면 변환 필요
        const priceEth = Number(item.currentPrice) || 0;

        return (
          <div key={auctionId}>
            <div className="h-3 bg-[#1F1F2D]" />
            {/* 상단: 배송상태 표시 */}
            <DeliveryBadge auctionId={auctionId} />

            <div className="flex justify-between border-b border-[#353535] px-6 py-4">
              <p className="text-sm">경매 번호 {item.code}</p>
              <p className="text-sm flex gap-1 items-center">
                경매종료 {item.endDatetime} <ChevronRight className="w-4" />
              </p>
            </div>

            <div className="py-4 px-6 flex justify-between">
              <div className="flex items-center gap-5">
                <div className="rounded-lg overflow-hidden w-22 h-22">
                  <Image
                    className="w-full h-full object-cover"
                    src={item.imageUrl || "/no-image.jpg"}
                    alt="thumbnail"
                    width={80}
                    height={80}
                    unoptimized
                  />
                </div>
                <div>
                  <h3 className="bid">{item.title}</h3>
                  <p className="text-lg">입찰가 {item.currentPrice} ETH</p>
                </div>
              </div>

              <div className="flex items-center gap-4">
                <div className="flex flex-col gap-3 items-end">
                  <p className="text-sm">현재 입찰가 <span className="text-green-500 ml-1">{item.currentPrice} ETH</span></p>
                  <p className="text-sm">내 입찰가 <span className="text-green-500 ml-1">{item.myTopBidAmount} ETH</span></p>

                  <div className="grid grid-cols-1 gap-3">
                    {/* 구매확정 버튼 (배송 완료 상태에서만 활성) */}
                    <ConfirmReceiptButton auctionId={auctionId} />
                  </div>
                </div>
              </div>
            </div>

            {/* 각 경매 카드별 PaySection */}
            <PaySectionWrapper auctionId={auctionId} priceEth={priceEth} />
          </div>
        );
      })}
    </div>
  );
}
