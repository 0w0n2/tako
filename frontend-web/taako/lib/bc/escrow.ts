// lib/bc/escrow.ts
import { Contract, parseEther } from "ethers";
import { getSigner } from "./provider";
import { ESCROW_ABI } from "./escrowAbi";

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

/** 구매자 배송 완료 후 구매확정 */
export const confirmReceipt = async (escrowAddress: `0x${string}`) => {
  const c = await getEscrow(escrowAddress);
  const tx = await c.confirmReceipt();
  return await tx.wait();
};

/** 판매자 정산(대금 인출) */
export const releaseFunds = async (escrowAddress: `0x${string}`) => {
  const c = await getEscrow(escrowAddress);
  const tx = await c.releaseFunds();
  return await tx.wait();
};
