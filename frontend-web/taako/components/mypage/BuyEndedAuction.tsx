// components/nft/BuyEndedAuction.tsx
"use client";

import Image from "next/image";
import { useState, useMemo, useCallback, useEffect } from "react";
import { ChevronRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useMyInfo } from "@/hooks/useMyInfo";
import ConfirmReceiptButton from "@/components/nft/ConfirmReceiptButton";
import { useDelivery } from "@/hooks/useDelivery";
import { useDelivery as useDelivery_2 } from "@/hooks/useSellDelivery";
import { useEscrowAddress } from "@/hooks/useEscrowAddress";
import { depositToEscrow } from "@/lib/bc/escrow";
import PayButton from "@/components/nft/PayButton"; // ← 경로 수정 완료
import SellDeliveryForm from "@/components/mypage/delivery/SellDeliveryForm";
import type { MyBidAuctions } from "@/types/auth";

const statusMap: Record<string, string> = {
  WAITING: "배송준비중",
  IN_PROGRESS: "배송중",
  COMPLETED: "배송완료",
  CONFIRMED: "구매확정",
};

function DeliveryBadge({ auctionId }: { auctionId: number }) {
  const { info, status, hasTracking } = useDelivery(auctionId);

  const label = useMemo(() => {
    if (!hasTracking) return "운송장 대기";
    switch (status) {
      case "WAITING":
        return "배송 준비중";
      case "IN_PROGRESS":
        return "배송중";
      case "COMPLETED":
        return "배송완료";
      case "CONFIRMED":
        return "구매확정됨";
      default:
        return status;
    }
  }, [hasTracking, status]);

  return (
    <div className="px-6 py-2 bg-[#171725] border-b border-[#353535] flex items-center justify-between">
      <p className="text-xs text-[#a5a5a5]">
        배송상태: <span className="text-[#e1e1e1]">{label}</span>
        {info?.trackingNumber ? (
          <span className="ml-2 text-[#8bb4ff]">#{info.trackingNumber}</span>
        ) : null}
      </p>
    </div>
  );
}

/** 경매 카드 1개 렌더링 */
function AuctionEndedRow({ item }: { item: MyBidAuctions }) {
  const auctionId = Number(item.auctionId);
  const [openAddressModal, setOpenAddressModal] = useState(false);
  const [addressRegistered, setAddressRegistered] = useState(false);
  const { auctionDelivery, handlerGetAuctionDelivery } = useDelivery_2();

  const { info } = useDelivery(auctionId);
  const trackingNumber = info?.trackingNumber ?? null;
  const trackingMissing = !((trackingNumber ?? "").trim().length > 0);
  const { data: escrowAddress } = useEscrowAddress(auctionId);

  // NOTE: 백엔드 금액 단위와 ETH 단위가 다르면 변환 필요
  const priceEth = useMemo(() => Number(item.currentPrice) || 0, [item.currentPrice]);

  const onPay = useCallback(async () => {
    if (!escrowAddress) {
      throw new Error("에스크로 주소를 가져오지 못했습니다.");
    }
    // PayButton 내부에서 에러 메시지를 보여주므로 여기서는 throw만 해도 됨.
    await depositToEscrow(escrowAddress, priceEth);
    // 결제 성공 후 서버 리포팅(API)이 필요하다면 여기에서 호출
  }, [escrowAddress, priceEth]);

  if (!Number.isFinite(auctionId)) {
    console.warn("유효하지 않은 auctionId", item);
    return null;
  }

  useEffect(()=> {
      handlerGetAuctionDelivery(item.auctionId);
    }, [])

  return (
    <div>
      <div className="h-3 bg-[#1F1F2D]" />

      {/* 배송 상태 */}
      <DeliveryBadge auctionId={auctionId} />

      {/* 상단 바 */}
      <div className="flex justify-between border-b border-[#353535] px-6 py-4">
        <p className="text-sm">경매 번호 {item.code}</p>
        <p className="text-sm flex gap-1 items-center">
          경매종료 {item.endDatetime} <ChevronRight className="w-4" />
        </p>
      </div>

      {/* 본문 */}
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
            {auctionDelivery?.status ? (
                <p className="text-sm text-green-500">
                  {statusMap[auctionDelivery?.status ?? ""]}
                </p>
              ) : (
                <p className="text-sm text-red-500">배송지 입력 대기</p>
              )}
          </div>
        </div>

        <div className="flex items-center gap-4">
          <div className="flex flex-col gap-3 items-end">
            <p className="text-sm">
              현재 입찰가{" "}
              <span className="text-green-500 ml-1">{item.currentPrice} ETH</span>
            </p>
            <p className="text-sm">
              내 입찰가{" "}
              <span className="text-green-500 ml-1">{item.myTopBidAmount} ETH</span>
            </p>

            {/* 버튼들: 오른쪽부터 [배송지 선택, 결제, 구매확정] */}
            <div className="flex justify-end items-center gap-3">
              {/* 1) 배송지 선택 버튼 (오른쪽) */}
              <Button
                variant="outline"
                className="min-w-[104px]"
                onClick={() => setOpenAddressModal(true)}
                disabled={addressRegistered}
              >
                {addressRegistered ? "배송지 선택 완료" : "배송지 선택"}
              </Button>

              {/* 2) 결제 버튼 (가운데) */}
              <PayButton
                auctionId={auctionId}
                trackingMissing={trackingMissing}
                disabledReason={trackingMissing ? '운송장 발급 후 결제 가능합니다.' : undefined}
                onPay={onPay}
                className="min-w-[104px]"
              />

              {/* 3) 구매확정 버튼 (왼쪽) */}
              <ConfirmReceiptButton auctionId={auctionId} />
            </div>
          </div>
        </div>
      </div>

      {/* 배송지 선택 모달 */}
      {openAddressModal && (
        <SellDeliveryForm
          auctionId={auctionId}
          onClose={() => setOpenAddressModal(false)}
        />
      )}
    </div>
  );
}

export default function BuyEndedAuction() {
  const { endedAuctions, myBidLoading, myBidError } = useMyInfo();

  if (myBidLoading)
    return (
      <div className="text-center text-[#a5a5a5] text-sm py-20">
        불러오는 중...
      </div>
    );

  if (myBidError)
    return (
      <div className="text-center text-[#a5a5a5] text-sm py-20">
        에러가 발생했습니다 😢
      </div>
    );

  if (!endedAuctions || endedAuctions.length === 0) {
    return (
      <div className="text-center text-[#a5a5a5] text-sm py-20">
        종료된 경매가 없습니다.
      </div>
    );
  }

  return (
    <div>
      {endedAuctions.map((item) => (
        <AuctionEndedRow key={String(item.auctionId)} item={item} />
      ))}
    </div>
  );
}
