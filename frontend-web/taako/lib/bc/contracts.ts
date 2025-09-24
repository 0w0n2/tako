// lib/eth/contracts.ts
import { Contract, InterfaceAbi, JsonRpcSigner } from "ethers";
import { getSigner } from "./provider";

export const ERC721_MIN_ABI: InterfaceAbi = [
  "function ownerOf(uint256 tokenId) view returns (address)",
  "function getApproved(uint256 tokenId) view returns (address)",
  "function isApprovedForAll(address owner, address operator) view returns (bool)",
  "function approve(address to, uint256 tokenId)",
  "function setApprovalForAll(address operator, bool _approved)",
  "event Approval(address indexed owner, address indexed approved, uint256 indexed tokenId)",
  "event ApprovalForAll(address indexed owner, address indexed operator, bool approved)"
];

export const TAKO_NFT_ABI: InterfaceAbi = [
  "function claim(uint256 tokenId, string secret) external",
  "function backendAdmin() view returns (address)",
  "function tokenSecrets(uint256 tokenId) view returns (bytes32)",
  "function usedSecrets(bytes32 h) view returns (bool)",
  ...ERC721_MIN_ABI, // ✅ ERC721 read/write 포함
];

export const getErc721 = async (nftAddress: `0x${string}`, signer?: JsonRpcSigner) => {
  const s = signer ?? (await getSigner());
  return new Contract(nftAddress, ERC721_MIN_ABI, s);
};

export const getTakoNft = async (nftAddress: `0x${string}`, signer?: JsonRpcSigner) => {
  const s = signer ?? (await getSigner());
  return new Contract(nftAddress, TAKO_NFT_ABI, s);
};
