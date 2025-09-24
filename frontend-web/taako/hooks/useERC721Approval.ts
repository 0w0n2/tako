// hooks/useERC721Approval.ts
'use client';

import { useCallback, useEffect, useMemo, useState } from 'react';
import { isAddress, Contract } from 'ethers';
import { getSigner, getAccount } from '@/lib/bc/provider';
import { ERC721_MIN_ABI } from '@/lib/bc/contracts';

type UseERC721ApprovalOptions = {
  nftAddress: string;            // ERC721 컨트랙트 주소
  tokenId: bigint | number;      // 토큰 ID
  spender: string;               // 승인 대상(필수 권장: 경매/에스크로 컨트랙트)
  requiredChainId?: number;      // ex) 11155111 (Sepolia)
};

export function useERC721Approval({
  nftAddress,
  tokenId,
  spender,
  requiredChainId,
}: UseERC721ApprovalOptions) {
  const [loading, setLoading] = useState(false);
  const [owner, setOwner] = useState<string>('');
  const [approved, setApproved] = useState<string>('0x0000000000000000000000000000000000000000');
  const [approvedForAll, setApprovedForAll] = useState<boolean>(false);
  const [error, setError] = useState<string>('');
  const [account, setAccount] = useState<string>('');

  const tid = useMemo(() => (typeof tokenId === 'bigint' ? tokenId : BigInt(tokenId)), [tokenId]);

  const validate = useCallback(() => {
    if (!isAddress(nftAddress)) throw new Error('잘못된 NFT 주소입니다.');
    if (!isAddress(spender)) throw new Error('승인 대상(spender) 주소가 유효하지 않습니다.');
  }, [nftAddress, spender]);

  const refresh = useCallback(async () => {
    setError('');
    try {
      validate();

      const signer = await getSigner(requiredChainId);
      const addr = await getAccount();
      setAccount(addr);

      const erc721 = new Contract(nftAddress, ERC721_MIN_ABI, signer);

      const ownerOf: string = await erc721.ownerOf(tid);
      const [currentApproved, isAll] = await Promise.all([
        erc721.getApproved(tid),
        erc721.isApprovedForAll(ownerOf, spender), // ✅ owner = ownerOf, operator = spender
      ]);

      setOwner(ownerOf);
      setApproved(currentApproved);
      setApprovedForAll(Boolean(isAll));
    } catch (e: any) {
      setError(e?.message ?? String(e));
    }
  }, [nftAddress, spender, tid, requiredChainId, validate]);

  useEffect(() => {
    if (!nftAddress || tokenId === undefined || !spender) return;
    void refresh();
  }, [nftAddress, tokenId, spender, refresh]);

  const approveToken = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      validate();

      const signer = await getSigner(requiredChainId);
      const erc721 = new Contract(nftAddress, ERC721_MIN_ABI, signer);

      if (owner && spender && owner.toLowerCase() === spender.toLowerCase()) {
        throw new Error('소유자 주소로는 approve 할 수 없습니다. (ERC721 규칙)');
      }

      const tx = await erc721.approve(spender, tid);
      const receipt = await tx.wait();
      await refresh();
      return receipt;
    } catch (e: any) {
      setError(e?.message ?? String(e));
      throw e;
    } finally {
      setLoading(false);
    }
  }, [nftAddress, spender, tid, owner, refresh, requiredChainId, validate]);

  const setApprovalForAll = useCallback(async (enable: boolean) => {
    setLoading(true);
    setError('');
    try {
      validate();

      const signer = await getSigner(requiredChainId);
      const erc721 = new Contract(nftAddress, ERC721_MIN_ABI, signer);

      if (owner && spender && owner.toLowerCase() === spender.toLowerCase()) {
        throw new Error('소유자 자신을 operator로 지정할 수 없습니다.');
      }

      const tx = await erc721.setApprovalForAll(spender, enable);
      const receipt = await tx.wait();
      await refresh();
      return receipt;
    } catch (e: any) {
      setError(e?.message ?? String(e));
      throw e;
    } finally {
      setLoading(false);
    }
  }, [nftAddress, spender, owner, refresh, requiredChainId, validate]);

  const isAlreadyApproved =
    !!spender &&
    (approved?.toLowerCase() === spender.toLowerCase() || approvedForAll);

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
    setApprovalForAll,
  };
}
