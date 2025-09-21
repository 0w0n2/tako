// lib/ethereum.ts
import { ethers, isAddress } from 'ethers';

// 메타마스크가 설치되어 있으면 BrowserProvider 반환, 아니면 null 반환
export function getEthereumProvider(): ethers.BrowserProvider | null {
  if (typeof window !== 'undefined' && window.ethereum) {
    return new ethers.BrowserProvider(window.ethereum);
  }
  return null;
}

// 사용자가 연결 요청할 때 호출하는 함수 (연결 요청 팝업 띄움)
export async function requestAccounts(): Promise<string[]> {
  const provider = getEthereumProvider();
  if (!provider) {
    throw new Error('MetaMask가 설치되어 있지 않습니다.');
  }
  // eth_requestAccounts RPC 호출
  const accounts = await provider.send('eth_requestAccounts', []);
  return accounts;
}

// 주소 유효성 검사 (옵션)
export function isValidAddress(wallet_address: string): boolean {
  return isAddress(wallet_address);
}
