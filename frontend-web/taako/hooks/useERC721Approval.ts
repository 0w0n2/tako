'use client';

import { useCallback, useEffect, useState } from 'react';
import { BrowserProvider, Contract, isAddress, formatEther } from 'ethers';
import { TAKO_NFT_ABI } from '@/lib/bc/takoNft';

type UseERC721ApprovalOptions = {
  nftAddress: string;      // ERC721 컨트랙트 주소
  tokenId: bigint;         // 토큰 ID (ethers v6는 bigint 권장)
  spender?: string;        // 기본 승인 대상(마켓/경매 컨트랙트)
};

export function useERC721Approval({ nftAddress, tokenId, spender }: UseERC721ApprovalOptions) {
  const [loading, setLoading] = useState(false);
  const [owner, setOwner] = useState<string>('');
  const [approved, setApproved] = useState<string>('0x0000000000000000000000000000000000000000');
  const [approvedForAll, setApprovedForAll] = useState<boolean>(false);
  const [error, setError] = useState<string>('');
  const [account, setAccount] = useState<string>('');

  const target = spender ?? process.env.NEXT_PUBLIC_TAKO_NFT ?? '';

  const getSigner = useCallback(async () => {
    if (typeof window === 'undefined' || !(window as any).ethereum) {
      throw new Error('MetaMask가 없습니다.');
    }
    const provider = new BrowserProvider((window as any).ethereum);
    await provider.send('eth_requestAccounts', []);
    const signer = await provider.getSigner();
    return signer;
  }, []);

  const refresh = useCallback(async () => {
    setError('');
    try {
      const signer = await getSigner();
      const addr = await signer.getAddress();
      setAccount(addr);

      if (!isAddress(nftAddress)) throw new Error('잘못된 NFT 주소');
      const erc721 = new Contract(nftAddress, TAKO_NFT_ABI, signer);

      const [ownerOf, currentApproved, isAll] = await Promise.all([
        erc721.ownerOf(tokenId),
        erc721.getApproved(tokenId),
        erc721.isApprovedForAll(addr, target || addr), // target 없으면 자기자신 vs 자기자신은 보통 false
      ]);

      setOwner(ownerOf);
      setApproved(currentApproved);
      setApprovedForAll(Boolean(target && await erc721.isApprovedForAll(ownerOf, target)));
    } catch (e: any) {
      setError(e?.message ?? String(e));
    }
  }, [getSigner, nftAddress, tokenId, target]);

  useEffect(() => {
    if (!nftAddress || tokenId === undefined) return;
    void refresh();
  }, [nftAddress, tokenId, refresh]);

  const approveToken = useCallback(async (customSpender?: string) => {
    setLoading(true);
    setError('');
    try {
      const signer = await getSigner();
      const erc721 = new Contract(nftAddress, TAKO_NFT_ABI, signer);
      const to = customSpender ?? target;
      if (!isAddress(to)) throw new Error('승인 대상 주소가 유효하지 않습니다.');
      if (to.toLowerCase() === owner.toLowerCase()) {
        throw new Error('소유자에게 approve 할 수 없습니다. (ERC721 규칙)');
      }
      const tx = await erc721.approve(to, tokenId);
      const receipt = await tx.wait();
      await refresh();
      return receipt;
    } catch (e: any) {
      setError(e?.message ?? String(e));
      throw e;
    } finally {
      setLoading(false);
    }
  }, [getSigner, nftAddress, tokenId, target, owner, refresh]);

  const setAll = useCallback(async (enable: boolean, customSpender?: string) => {
    setLoading(true);
    setError('');
    try {
      const signer = await getSigner();
      const erc721 = new Contract(nftAddress, TAKO_NFT_ABI, signer);
      const to = customSpender ?? target;
      if (!isAddress(to)) throw new Error('운영자 주소가 유효하지 않습니다.');
      if (to.toLowerCase() === owner.toLowerCase()) {
        throw new Error('소유자 자신을 operator로 지정할 수 없습니다.');
      }
      const tx = await erc721.setApprovalForAll(to, enable);
      const receipt = await tx.wait();
      await refresh();
      return receipt;
    } catch (e: any) {
      setError(e?.message ?? String(e));
      throw e;
    } finally {
      setLoading(false);
    }
  }, [getSigner, nftAddress, tokenId, target, owner, refresh]);

  const isAlreadyApproved =
    !!target &&
    (approved?.toLowerCase() === target.toLowerCase() || approvedForAll);

  return {
    account,
    owner,
    approved,
    approvedForAll,
    isAlreadyApproved,
    loading,
    error,
    refresh,
    approveToken,
    setApprovalForAll: setAll,
  };
}
