// lib/bc/provider.ts
import { BrowserProvider, Eip1193Provider, JsonRpcSigner } from "ethers";

/** 기본 체인: Sepolia (11155111) — 필요 시 환경변수로 덮어쓰기 */
const DEFAULT_CHAIN_ID = Number(process.env.NEXT_PUBLIC_CHAIN_ID ?? 11155111);

/** 10진 체인ID → 0x Hex 문자열 */
const toHexChainId = (chainId: number) => {
  if (!Number.isInteger(chainId)) throw new Error(`잘못된 chainId: ${chainId}`);
  return `0x${chainId.toString(16)}`;
};

/** 알려진 체인의 wallet_addEthereumChain 파라미터 */
const getKnownChainParams = (chainId: number) => {
  if (chainId === 11155111) {
    return {
      chainId: toHexChainId(chainId),
      chainName: "Sepolia",
      nativeCurrency: { name: "ETH", symbol: "ETH", decimals: 18 },
      rpcUrls: ["https://rpc.sepolia.org"],
      blockExplorerUrls: ["https://sepolia.etherscan.io"],
    };
  }
  // 다른 체인을 쓰면 여기에 추가하거나 .env에서 동적으로 받아도 됨
  return {
    chainId: toHexChainId(chainId),
    chainName: `EVM Chain ${chainId}`,
    nativeCurrency: { name: "ETH", symbol: "ETH", decimals: 18 },
    rpcUrls: [],
    blockExplorerUrls: [],
  };
};

export const getBrowserProvider = () => {
  const eth = (globalThis as any).ethereum as Eip1193Provider | undefined;
  if (!eth) throw new Error("MetaMask가 필요합니다.");
  return new BrowserProvider(eth);
};

export const requestAccounts = async () => {
  const provider = getBrowserProvider();
  // 계정 연결 요청 (승인 창)
  await provider.send("eth_requestAccounts", []);
  return provider;
};

/**
 * 요구 체인 보장 (기본: Sepolia)
 * - 현재 네트워크가 다르면 wallet_switchEthereumChain
 * - 미등록 체인이면 wallet_addEthereumChain
 */
export const ensureChain = async (requiredChainId: number = DEFAULT_CHAIN_ID) => {
  const provider = await requestAccounts();

  const nw = await provider.getNetwork(); // ethers v6: { chainId: bigint }
  const current = Number(nw.chainId);

  if (current === requiredChainId) return provider;

  const chainIdHex = toHexChainId(requiredChainId);

  try {
    await provider.send("wallet_switchEthereumChain", [{ chainId: chainIdHex }]);
  } catch (e: any) {
    // 4902: Unrecognized chain → add
    if (e?.code === 4902) {
      const params = getKnownChainParams(requiredChainId);
      await provider.send("wallet_addEthereumChain", [params]);
    } else {
      throw e;
    }
  }
  return provider;
};

export const getSigner = async (requiredChainId?: number): Promise<JsonRpcSigner> => {
  const provider = await ensureChain(requiredChainId);
  const signer = await provider.getSigner();
  // 일부 지갑은 계정 미연결 시 signer.getAddress()에서 throw → 선검증
  try {
    await signer.getAddress();
  } catch {
    // 계정 연결 재요청
    await provider.send("eth_requestAccounts", []);
  }
  return signer;
};

export const getAccount = async (): Promise<`0x${string}`> => {
  const signer = await getSigner();
  const addr = await signer.getAddress();
  return addr as `0x${string}`;
};
