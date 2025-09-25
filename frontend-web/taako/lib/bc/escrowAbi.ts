// lib/bc/escrowAbi.ts
export const ESCROW_ABI = [
  // 입금/확정/정산
  "function deposit() external payable",
  "function confirmReceipt() external",
  "function releaseFunds() external",
] as const;
