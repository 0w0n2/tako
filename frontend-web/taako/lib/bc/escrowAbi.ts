// lib/bc/escrowAbi.ts
export const ESCROW_ABI = [
  // 입금/확정/정산
  "function deposit() external payable",
  "function confirmReceipt() external",
  "function releaseFunds() external",
  
  // currentState 읽기
  "function currentState() view returns (uint8)",
] as const;

// 상태 헬퍼(선택)
export const ESCROW_STATE = {
  DEPOSIT_PENDING: 0,
  CONFIRM_PENDING: 1,
  COMPLETED: 2, // 구매자 confirmReceipt 완료 이후
  CANCELED: 3,
} as const;