import type { InterfaceAbi } from "ethers";

// 최소 ABI: payable deposit(), confirmReceipt()
export const ESCROW_ABI: InterfaceAbi = [
  "function deposit() external payable",
  "function confirmReceipt() external",
  // 조회/이벤트가 있다면 여기에 추가
];
