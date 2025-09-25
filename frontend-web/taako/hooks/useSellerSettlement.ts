// hooks/useSellerSettlement.ts
"use client";

import { useMemo } from "react";
import { useMutation } from "@tanstack/react-query";
import { useDelivery } from "@/hooks/useDelivery";
import { useEscrowAddress } from "@/hooks/useEscrowAddress";
import { useERC721Approval } from "@/hooks/useERC721Approval";
import { releaseFunds } from "@/lib/bc/escrow";
// import type { DeliveryStatus } from "@/types/delivery"; // ← 더 이상 직접 선언 안 쓰면 불필요

type Params = {
  auctionId: number;
  nftAddress: `0x${string}`;
  tokenId: number | string | bigint;
  sellerWallet?: `0x${string}`;
  preferForAll?: boolean;
  addressId?: number;
};

export function useSellerSettlement({
  auctionId,
  nftAddress,
  tokenId,
  sellerWallet,
  preferForAll = true,
  addressId,
}: Params) {
  // 1) 에스크로 주소
  const { data: escrowAddress, isLoading: escrowLoading, error: escrowError } =
    useEscrowAddress(auctionId);

  // 2) 배송 상태 (구매확정 확인) - options 객체로 전달
  const {
    info: delivery,
    loading: deliveryLoading,
    error: deliveryError,
    status: deliveryStatus, // ← 이름을 바꿔 받기
  } = useDelivery(auctionId, { addressId });

  // 전달받은 status를 그대로 사용
  const isConfirmed = deliveryStatus === "CONFIRMED";

  // 3) 승인 상태 훅
  const approval = useERC721Approval({
    nftAddress,
    tokenId: typeof tokenId === "bigint" ? tokenId : BigInt(tokenId),
    spender: (escrowAddress ?? "0x0000000000000000000000000000000000000000") as string,
  });

  const canApprove = useMemo(() => Boolean(escrowAddress), [escrowAddress]);
  const canRelease = useMemo(() => Boolean(escrowAddress) && isConfirmed, [escrowAddress, isConfirmed]);

  // 4) 승인의 실제 실행
  const approveMutation = useMutation({
    mutationFn: async () => {
      if (!escrowAddress) throw new Error("에스크로 주소를 불러오지 못했습니다.");
      if (preferForAll) {
        return await approval.setApprovalForAll(true);
      }
      return await approval.approveToken();
    },
  });

  // 5) 정산 실행
  const releaseMutation = useMutation({
    mutationFn: async () => {
      if (!escrowAddress) throw new Error("에스크로 주소를 불러오지 못했습니다.");
      if (!isConfirmed) throw new Error("구매자가 아직 '구매 확정'을 완료하지 않았습니다.");
      return await releaseFunds(escrowAddress as `0x${string}`);
    },
  });

  const humanize = (e: unknown) => {
    const msg = (e as Error)?.message ?? String(e);
    if (/user rejected|rejected by user/i.test(msg)) return "사용자가 서명을 거부했습니다.";
    if (/insufficient funds/i.test(msg)) return "가스비(ETH)가 부족합니다.";
    if (/MetaMask|ethereum/i.test(msg)) return "MetaMask 연결이 필요합니다.";
    if (/구매 확정/.test(msg)) return msg;
    return `오류가 발생했습니다: ${msg}`;
  };

  return {
    // 상태
    escrowAddress,
    status: deliveryStatus, // ← 노출은 그대로 status로
    isConfirmed,
    escrowLoading,
    deliveryLoading,
    escrowError,
    deliveryError,

    // 승인 관련
    alreadyApproved: approval.isAlreadyApproved,
    approving: approveMutation.isPending,
    canApprove,

    // 정산 관련
    releasing: releaseMutation.isPending,
    canRelease,

    // 액션
    approve: async () => {
      try {
        await approveMutation.mutateAsync();
        await approval.refresh();
        return { ok: true, message: "승인이 완료되었습니다." };
      } catch (e) {
        return { ok: false, message: humanize(e) };
      }
    },
    release: async () => {
      try {
        await releaseMutation.mutateAsync();
        return { ok: true, message: "대금 인출이 완료되었습니다." };
      } catch (e) {
        return { ok: false, message: humanize(e) };
      }
    },
  };
}
