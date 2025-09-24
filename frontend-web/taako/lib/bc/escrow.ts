import { Contract, parseEther } from "ethers";
import { getSigner } from "./provider";

export const ESCROW_ABI = [
  "function deposit() external payable",
  "function confirmReceipt() external",
] as const;

export const getEscrow = async (escrowAddress: `0x${string}`) => {
  const signer = await getSigner();
  return new Contract(escrowAddress, ESCROW_ABI, signer);
};

/** 에스크로에 ETH 입금 (0.01 등 소수점 가능) */
export const depositToEscrow = async (
  escrowAddress: `0x${string}`,
  amountEth: string | number
) => {
  const c = await getEscrow(escrowAddress);
  const tx = await c.deposit({ value: parseEther(String(amountEth)) });
  return await tx.wait();
};

/** 배송 완료 후 구매확정 */
export const confirmReceipt = async (escrowAddress: `0x${string}`) => {
  const c = await getEscrow(escrowAddress);
  const tx = await c.confirmReceipt();
  return await tx.wait();
};
