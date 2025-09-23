// lib/eth/takoNft.ts
import { Contract, InterfaceAbi, BrowserProvider, Eip1193Provider } from "ethers";

export const TAKO_NFT_ABI: InterfaceAbi = [
  "function claim(uint256 tokenId, string secret) external",
  // 조회용(있으면 좋음; public이면 자동 getter 존재)
  "function backendAdmin() view returns (address)",
  "function tokenSecrets(uint256 tokenId) view returns (bytes32)", // public일 때만 동작
  "function usedSecrets(bytes32 h) view returns (bool)",           // public일 때만 동작
  // read
  "function ownerOf(uint256 tokenId) view returns (address)",
  "function getApproved(uint256 tokenId) view returns (address)",
  "function isApprovedForAll(address owner, address operator) view returns (bool)",
  // write
  "function approve(address to, uint256 tokenId) external",
  "function setApprovalForAll(address operator, bool _approved) external",
  // events (선택)
  "event Approval(address indexed owner, address indexed approved, uint256 indexed tokenId)",
  "event ApprovalForAll(address indexed owner, address indexed operator, bool approved)"
];

export const getSigner = async () => {
  const eth = (globalThis as any).ethereum as Eip1193Provider | undefined;
  if (!eth) throw new Error("MetaMask가 필요합니다.");
  const provider = new BrowserProvider(eth);
  await provider.send("eth_requestAccounts", []);
  return provider.getSigner();
};

export const getTakoNft = async (address: `0x${string}`) => {
  const signer = await getSigner();
  return new Contract(address, TAKO_NFT_ABI, signer);
};
