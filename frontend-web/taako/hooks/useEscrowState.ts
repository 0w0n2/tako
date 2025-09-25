// hooks/useEscrowState.ts
"use client";

import { useQuery } from "@tanstack/react-query";
import { Contract, BrowserProvider } from "ethers";
import { useEscrowAddress } from "@/hooks/useEscrowAddress";
import { ESCROW_ABI, ESCROW_STATE } from "@/lib/bc/escrowAbi";

type EscrowStateData = {
  state: number; // 0,1,2,3
};

export function useEscrowState(auctionId: number, poll = true, intervalMs = 10_000) {
  const { data: escrowAddress } = useEscrowAddress(auctionId);

  const q = useQuery<EscrowStateData>({
    queryKey: ["escrowState", auctionId, escrowAddress],
    enabled: !!auctionId && !!escrowAddress,
    queryFn: async () => {
      if (!escrowAddress) throw new Error("에스크로 주소가 없습니다.");

      const eth = (window as any).ethereum;
      if (!eth) throw new Error("MetaMask가 필요합니다.");

      const provider = new BrowserProvider(eth);
      const escrow = new Contract(escrowAddress, ESCROW_ABI, provider);
      const state: bigint = await escrow.currentState();
      return { state: Number(state) };
    },
    refetchInterval: (query) => {
      if (!poll) return false;
      // query.state.data 는 EscrowStateData | undefined
      const s = (query.state.data as { state: number } | undefined)?.state;
      return s === ESCROW_STATE.COMPLETED || s === ESCROW_STATE.CANCELED ? false : intervalMs;
    },

    staleTime: 10_000,
    gcTime: 5 * 60 * 1000,
  });

  return {
    escrowAddress,
    state: q.data?.state, // 0|1|2|3
    loading: q.isLoading,
    error: q.isError ? (q.error instanceof Error ? q.error.message : "Unknown error") : "",
    refetch: q.refetch,
  };
}
