// components/nft/SellerPanel.tsx
"use client";

import { useMemo, useState } from "react";
import { Button } from "@/components/ui/button";
import { CheckCircle, ShieldCheck, Wallet } from "lucide-react";
import useWallet from "@/hooks/useWallet";
import { useSellerSettlement } from "@/hooks/useSellerSettlement";
import { ESCROW_STATE } from "@/lib/bc/escrowAbi";

type Props = {
  auctionId: number;
  sellerWallet?: `0x${string}`;
  preferForAll?: boolean;
  addressId?: number;
};

export default function SellerPayoutPanel({
  auctionId,
  sellerWallet,
  preferForAll = true,
}: Props) {
  const { walletAddress, error: walletError, loading: walletLoading } = useWallet();

  const {
    escrowAddress,
    escrowState,
    buyerConfirmed,
    escrowLoading,
    escrowError,

    nftAddress,
    tokenId,
    nftNotMinted,

    alreadyApproved,
    approving,
    canApprove,

    releasing,
    canRelease,
    approve,
    release,
  } = useSellerSettlement({
    auctionId,
    sellerWallet,
    preferForAll,
  });

  const [msg, setMsg] = useState("");

  const walletMismatch = useMemo(() => {
    if (!sellerWallet || !walletAddress) return false;
    return sellerWallet.toLowerCase() !== walletAddress.toLowerCase();
  }, [sellerWallet, walletAddress]);

  const handleApprove = async () => {
    if (walletMismatch) return setMsg("판매자 등록 지갑과 현재 지갑이 일치하지 않습니다.");
    const r = await approve();
    setMsg(r.message);
  };

  const handleRelease = async () => {
    if (walletMismatch) return setMsg("판매자 등록 지갑과 현재 지갑이 일치하지 않습니다.");
    const r = await release();
    setMsg(r.message);
  };

  const escrowBadge = (() => {
    switch (escrowState) {
      case ESCROW_STATE.AwaitingPayment: return <span className="text-xs px-2 py-1 rounded bg-[#2b2b3a]">입금 대기</span>;
      case ESCROW_STATE.AwaitingConfirmation: return <span className="text-xs px-2 py-1 rounded bg-[#2b2b3a]">구매자 확인 대기</span>;
      case ESCROW_STATE.Complete: return <span className="text-xs px-2 py-1 rounded bg-green-700/40">구매확정됨</span>;
      case ESCROW_STATE.Canceled: return <span className="text-xs px-2 py-1 rounded bg-red-800/40">취소됨</span>;
      default: return <span className="text-xs px-2 py-1 rounded bg-[#2b2b3a]">-</span>;
    }
  })();

  // 버튼 비활성 조건(최종)
  const disabledApprove =
    walletLoading || escrowLoading || approving || !canApprove || walletMismatch;

  const disabledRelease =
    walletLoading || escrowLoading || releasing || !canRelease || walletMismatch;

  return (
    <div className="w-full rounded-xl bg-[#191924] border border-[#353535] p-5 space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-white text-lg font-semibold">판매자 정산</h3>
        <div className="flex items-center gap-2">
          <span className="text-sm text-[#b5b5b5]">에스크로</span>
          {escrowBadge}
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
        <div className="rounded-xl border border-[#353535] p-4">
          <div className="text-sm text-[#b5b5b5] mb-1">에스크로 주소</div>
          <div className="text-xs break-all text-[#dedede]">
            {escrowAddress ?? "불러오는 중..."}
          </div>
          {escrowError && <div className="text-xs text-red-400 mt-1">{String(escrowError)}</div>}
        </div>

        <div className="rounded-xl border border-[#353535] p-4">
          <div className="text-sm text-[#b5b5b5] mb-1">내 지갑</div>
          <div className="text-xs break-all text-[#dedede]">
            {walletAddress ?? "연결 필요"}
          </div>
          {walletMismatch && (
            <div className="text-xs text-red-400 mt-1">판매자 등록 지갑과 일치하지 않습니다.</div>
          )}
          {walletError && <div className="text-xs text-red-400 mt-1">{walletError}</div>}
        </div>

        <div className="rounded-xl border border-[#353535] p-4">
          <div className="text-sm text-[#b5b5b5] mb-1">NFT / Token</div>
          <div className="text-xs text-[#dedede] break-all">{nftAddress}</div>
          <div className="text-xs text-[#b5b5b5]">Token #{String(tokenId ?? 0)}</div>
          {nftNotMinted && (
            <div className="text-xs text-amber-300 mt-1">
              이 경매의 NFT가 아직 발급되지 않았습니다. 승인 없이 바로 정산할 수 있습니다.
            </div>
          )}
          {!nftNotMinted && alreadyApproved && (
            <div className="text-xs text-green-400 mt-1">이미 승인됨</div>
          )}
        </div>
      </div>

      <div className="flex flex-col md:flex-row gap-3">
        <Button
          className="flex-1 bg-[#242433] hover:bg-[#2c2c3b] text-white disabled:opacity-60"
          disabled={disabledApprove}
          onClick={handleApprove}
          title={
            nftNotMinted
              ? "NFT가 아직 발급되지 않아 승인할 필요가 없습니다."
              : buyerConfirmed
                ? (alreadyApproved ? "이미 승인되었습니다." : "")
                : "구매자가 confirmReceipt를 해야 승인할 수 있습니다."
          }
        >
          <ShieldCheck className="w-4 h-4 mr-2" />
          NFT 소유권 이전 승인 ({preferForAll ? "모든 토큰" : `토큰 #${String(tokenId ?? 0)}`})
        </Button>

        <Button
          className="flex-1 bg-green-700 hover:bg-green-800 text-white disabled:bg-green-900/40"
          disabled={disabledRelease}
          onClick={handleRelease}
          title={
            buyerConfirmed
              ? (nftNotMinted ? "" : (alreadyApproved ? "" : "먼저 NFT 승인(approve)을 완료해주세요."))
              : "구매자가 confirmReceipt를 해야 정산 가능합니다."
          }
        >
          <Wallet className="w-4 h-4 mr-2" />
          대금 인출(Release Funds)
        </Button>
      </div>

      {!!msg && (
        <div className="flex items-center gap-2 text-sm text-[#dedede]">
          <CheckCircle className="w-4 h-4" /> {msg}
        </div>
      )}
    </div>
  );
}
