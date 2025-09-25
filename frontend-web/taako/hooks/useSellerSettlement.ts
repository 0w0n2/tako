// hooks/useSellerSettlement.ts
"use client";

import { useMemo } from "react";
import { useMutation } from "@tanstack/react-query";
import { useEscrowAddress } from "@/hooks/useEscrowAddress";
import { useEscrowState } from "@/hooks/useEscrowState";
import { useERC721Approval } from "@/hooks/useERC721Approval";
import { releaseFunds } from "@/lib/bc/escrow";
import { ESCROW_STATE } from "@/lib/bc/escrowAbi";

type Params = {
  auctionId: number;
  nftAddress: string;
  tokenId: number | string | bigint;
  sellerWallet?: `0x${string}`;
  preferForAll?: boolean;
};

export function useSellerSettlement({
  auctionId,
  nftAddress,
  tokenId,
  sellerWallet,
  preferForAll = true,
}: Params) {
  // 1) 에스크로 주소
  const { data: escrowAddress, isLoading: escrowLoading, error: escrowError } =
    useEscrowAddress(auctionId);

  // 2) 에스크로 상태
  const { state: escrowState, loading: stateLoading, error: stateError, refetch: refetchState } =
    useEscrowState(auctionId, true, 10_000);

  const buyerConfirmed = escrowState === ESCROW_STATE.COMPLETED; // = 2

  // hooks/useSellerSettlement.ts
  const approval = useERC721Approval({
    nftAddress,
    tokenId:
      tokenId !== undefined
        ? (typeof tokenId === "bigint" ? tokenId : BigInt(tokenId))
        : BigInt(0), // ← 0n 대신 BigInt(0)
    spender: (escrowAddress ?? "0x0000000000000000000000000000000000000000") as string,
  });

  // 버튼 활성 조건
  // - approve: buyerConfirmed && !alreadyApproved
  // - release: buyerConfirmed && alreadyApproved
  const canApprove = useMemo(
    () => Boolean(escrowAddress) && buyerConfirmed && !approval.isAlreadyApproved,
    [escrowAddress, buyerConfirmed, approval.isAlreadyApproved]
  );

  const canRelease = useMemo(
    () => Boolean(escrowAddress) && buyerConfirmed && approval.isAlreadyApproved,
    [escrowAddress, buyerConfirmed, approval.isAlreadyApproved]
  );

  // 승인 실행
  const approveMutation = useMutation({
    mutationFn: async () => {
      if (!escrowAddress) throw new Error("에스크로 주소를 불러오지 못했습니다.");
      if (preferForAll) return await approval.setApprovalForAll(true);
      return await approval.approveToken();
    },
    onSuccess: () => {
      void approval.refresh();
    },
  });

  // 정산 실행
  const releaseMutation = useMutation({
    mutationFn: async () => {
      if (!escrowAddress) throw new Error("에스크로 주소를 불러오지 못했습니다.");
      if (!approval.isAlreadyApproved) throw new Error("먼저 NFT 승인(approve)을 완료해주세요.");
      const receipt = await releaseFunds(escrowAddress as `0x${string}`);
      // 상태 변화가 있으면 갱신
      void refetchState();
      return receipt;
    },
  });

  const humanize = (e: unknown) => {
    const msg = (e as Error)?.message ?? String(e);
    if (/user rejected|rejected by user/i.test(msg)) return "사용자가 서명을 거부했습니다.";
    if (/insufficient funds/i.test(msg)) return "가스비(ETH)가 부족합니다.";
    if (/MetaMask|ethereum/i.test(msg)) return "MetaMask 연결이 필요합니다.";
    if (/confirmReceipt|구매자가 아직/.test(msg)) return msg;
    if (/approve/.test(msg)) return "먼저 NFT 승인(approve)을 완료해주세요.";
    return `오류가 발생했습니다: ${msg}`;
  };

  return {
    // 상태
    escrowAddress,
    escrowState,               // 0/1/2/3
    buyerConfirmed,            // currentState === 2
    escrowLoading: escrowLoading || stateLoading,
    escrowError: escrowError || stateError || "",

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
