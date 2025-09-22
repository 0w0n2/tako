// lib/ethereum.ts
import { BrowserProvider, isAddress } from 'ethers';
import type { MetaMaskInpageProvider } from '@metamask/providers';

/** 멀티지갑 환경에서 메타마스크 provider 선택 */
export function getMetaMaskProvider():
  (MetaMaskInpageProvider & { isMetaMask?: boolean }) | undefined {
  if (typeof window === 'undefined') return undefined;
  const eth: any = (window as any).ethereum;
  if (!eth) return undefined;
  if (Array.isArray(eth?.providers)) {
    // 여러 확장(OKX, Coinbase 등) 중 메타마스크 골라잡기
    return eth.providers.find((p: any) => p?.isMetaMask);
  }
  return eth;
}

/** 메타마스크가 설치되어 있으면 BrowserProvider, 아니면 null */
export function getBrowserProvider(): BrowserProvider | null {
  const mm = getMetaMaskProvider();
  if (!mm) return null;
  return new BrowserProvider(mm as any);
}

/** 계정 요청(팝업) */
export async function requestAccounts(): Promise<string[]> {
  const provider = getBrowserProvider();
  if (!provider) throw new Error('MetaMask가 설치되어 있지 않습니다.');
  return provider.send('eth_requestAccounts', []);
}

/** 주소 유효성 검사 */
export function isValidAddress(addr: string): boolean {
  return isAddress(addr);
}

/** 사람이 읽기 쉬운 체인 이름 */
export function friendlyChainName(chainId: bigint, fallback?: string) {
  if (chainId === 1n) return 'mainnet';
  if (chainId === 11155111n) return 'sepolia';
  return fallback || `chain(${chainId})`;
}

/** (옵션) 네트워크 전환 지원 — 필요 없으면 export 제거해도 OK */
const NETWORKS = {
  mainnet: {
    chainIdHex: '0x1',
    chainName: 'Ethereum Mainnet',
    rpcUrls: ['https://rpc.ankr.com/eth'],
    blockExplorerUrls: ['https://etherscan.io'],
    nativeCurrency: { name: 'Ether', symbol: 'ETH', decimals: 18 },
  },
  sepolia: {
    chainIdHex: '0xAA36A7', // 11155111
    chainName: 'Sepolia Test Network',
    rpcUrls: ['https://rpc.sepolia.org'],
    blockExplorerUrls: ['https://sepolia.etherscan.io'],
    nativeCurrency: { name: 'Sepolia ETH', symbol: 'SEP', decimals: 18 },
  },
} as const;
export type NetworkKey = keyof typeof NETWORKS;

export async function switchNetwork(key: NetworkKey) {
  const mm = getMetaMaskProvider();
  if (!mm) throw new Error('MetaMask가 설치되어 있지 않습니다.');
  const target = NETWORKS[key];

  try {
    await (mm as any).request({
      method: 'wallet_switchEthereumChain',
      params: [{ chainId: target.chainIdHex }],
    });
  } catch (e: any) {
    // 4902: 지갑에 네트워크가 없으면 추가 후 재시도
    if (e?.code === 4902) {
      await (mm as any).request({
        method: 'wallet_addEthereumChain',
        params: [{
          chainId: target.chainIdHex,
          chainName: target.chainName,
          rpcUrls: target.rpcUrls,
          blockExplorerUrls: target.blockExplorerUrls,
          nativeCurrency: target.nativeCurrency,
        }],
      });
      await (mm as any).request({
        method: 'wallet_switchEthereumChain',
        params: [{ chainId: target.chainIdHex }],
      });
    } else {
      throw e;
    }
  }
}
