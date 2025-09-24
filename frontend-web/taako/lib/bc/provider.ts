// lib/eth/provider.ts
import { BrowserProvider, Eip1193Provider, JsonRpcSigner } from "ethers";

export const getBrowserProvider = () => {
  const eth = (globalThis as any).ethereum as Eip1193Provider | undefined;
  if (!eth) throw new Error("MetaMask가 필요합니다.");
  return new BrowserProvider(eth);
};

export const requestAccounts = async () => {
  const provider = getBrowserProvider();
  await provider.send("eth_requestAccounts", []);
  return provider;
};

/** (옵션) 지정 체인 보장: ex) Sepolia 11155111 */
export const ensureChain = async (requiredChainId?: number) => {
  const provider = await requestAccounts();
  if (!requiredChainId) return provider;

  const nw = await provider.getNetwork();
  if (Number(nw.chainId) !== requiredChainId) {
    try {
      await provider.send("wallet_switchEthereumChain", [
        { chainId: "0xaa36a7" }, // 11155111(Sepolia)
      ]);
    } catch (e: any) {
      if (e?.code === 4902) {
        await provider.send("wallet_addEthereumChain", [{
          chainId: "0xaa36a7",
          chainName: "Sepolia",
          nativeCurrency: { name: "ETH", symbol: "ETH", decimals: 18 },
          rpcUrls: ["https://rpc.sepolia.org"],
          blockExplorerUrls: ["https://sepolia.etherscan.io"],
        }]);
      } else {
        throw e;
      }
    }
  }
  return provider;
};

export const getSigner = async (requiredChainId?: number): Promise<JsonRpcSigner> => {
  const provider = await ensureChain(requiredChainId);
  return provider.getSigner();
};

export const getAccount = async () => {
  const signer = await getSigner();
  return signer.getAddress();
};
