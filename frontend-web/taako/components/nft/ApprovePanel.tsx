'use client';

import { useCallback, useEffect, useMemo, useState } from 'react';
import { BrowserProvider, Contract, isAddress } from 'ethers';
import { TAKO_NFT_ABI } from '@/lib/bc/takoNft';

type Props = {
  /** ERC-721 컨트랙트 주소 */
  nftAddress: string;
  /** 대상 토큰 ID */
  tokenId: number | string | bigint;
  /** 경매/마켓 컨트랙트 주소 (없으면 env 사용) */
  auctionAddress?: string;
  /** 버튼 라벨/문구 커스터마이즈 (선택) */
  labels?: Partial<{
    title: string;
    approveSingle: string;
    revokeSingle: string;
    setAllOn: string;
    setAllOff: string;
    refresh: string;
  }>;
  /** 패널 컴팩트 모드 (버튼 간격 축소) */
  compact?: boolean;
};

export default function ApprovePanel({
  nftAddress,
  tokenId,
  auctionAddress,
  labels,
  compact = false,
}: Props) {
  const [account, setAccount] = useState('');
  const [owner, setOwner] = useState('');
  const [approved, setApproved] = useState<string>('0x0000000000000000000000000000000000000000');
  const [approvedForAll, setApprovedForAll] = useState(false);
  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState('');
  const [err, setErr] = useState('');

  const spender = useMemo(
    () => auctionAddress ?? process.env.NEXT_PUBLIC_SPENDER_ADDRESS ?? '',
    [auctionAddress]
  );
  const tid = typeof tokenId === 'bigint' ? tokenId : BigInt(tokenId);

  const L = {
    title: labels?.title ?? 'NFT 승인 패널',
    approveSingle: labels?.approveSingle ?? '이 NFT 승인하기',
    revokeSingle: labels?.revokeSingle ?? '이 NFT 단건 승인 해지',
    setAllOn: labels?.setAllOn ?? '모든 NFT 전역 승인 (SetApprovalForAll)',
    setAllOff: labels?.setAllOff ?? '전역 승인 해지',
    refresh: labels?.refresh ?? '상태 새로고침',
  };

  const getSigner = useCallback(async () => {
    if (typeof window === 'undefined' || !(window as any).ethereum) {
      throw new Error('MetaMask가 없습니다.');
    }
    const provider = new BrowserProvider((window as any).ethereum);
    // 계정 요청/연결
    await provider.send('eth_requestAccounts', []);
    return provider.getSigner();
  }, []);

  const readState = useCallback(async () => {
    setErr('');
    try {
      if (!isAddress(nftAddress)) throw new Error('잘못된 NFT 컨트랙트 주소입니다.');
      const signer = await getSigner();
      const addr = await signer.getAddress();
      setAccount(addr);

      const erc721 = new Contract(nftAddress, TAKO_NFT_ABI, signer);
      const [ownerOf, currentApproved, isAll] = await Promise.all([
        erc721.ownerOf(tid),
        erc721.getApproved(tid),
        isAddress(spender) ? erc721.isApprovedForAll(addr, spender) : Promise.resolve(false),
      ]);

      setOwner(ownerOf);
      setApproved(currentApproved);
      setApprovedForAll(Boolean(isAll));
    } catch (e: any) {
      setErr(e?.message ?? String(e));
    }
  }, [getSigner, nftAddress, tid, spender]);

  useEffect(() => {
    void readState();
  }, [readState]);

  // 체인/계정 변경 이벤트 대응
  useEffect(() => {
    const eth = (window as any)?.ethereum;
    if (!eth?.on) return;

    const handleAccountsChanged = () => void readState();
    const handleChainChanged = () => void readState();

    eth.on('accountsChanged', handleAccountsChanged);
    eth.on('chainChanged', handleChainChanged);
    return () => {
      try {
        eth.removeListener('accountsChanged', handleAccountsChanged);
        eth.removeListener('chainChanged', handleChainChanged);
      } catch {}
    };
  }, [readState]);

  const isOwner = useMemo(
    () => owner && account && owner.toLowerCase() === account.toLowerCase(),
    [owner, account]
  );

  const alreadyApproved = useMemo(() => {
    if (!spender || !isAddress(spender)) return false;
    return (
      approved?.toLowerCase() === spender.toLowerCase() ||
      approvedForAll
    );
  }, [approved, approvedForAll, spender]);

  const withTx = useCallback(
    async (fn: () => Promise<any>, successMsg: string) => {
      setLoading(true);
      setErr('');
      setMsg('');
      try {
        const receipt = await fn();
        setMsg(`${successMsg} (tx: ${receipt?.hash?.slice(0, 10)}...)`);
        await readState();
      } catch (e: any) {
        setErr(e?.message ?? String(e));
      } finally {
        setLoading(false);
      }
    },
    [readState]
  );

  const doApproveSingle = useCallback(async () => {
    if (!isOwner) return setErr('소유자만 승인할 수 있습니다.');
    if (!isAddress(spender)) return setErr('유효한 경매/마켓 컨트랙트 주소가 필요합니다.');
    if (spender.toLowerCase() === owner.toLowerCase()) return setErr('소유자에게는 approve 할 수 없습니다.');
    const signer = await getSigner();
    const erc721 = new Contract(nftAddress, TAKO_NFT_ABI, signer);
    return withTx(() => erc721.approve(spender, tid), '단건 승인 완료');
  }, [TAKO_NFT_ABI, getSigner, isOwner, nftAddress, owner, spender, tid, withTx]);

  const doRevokeSingle = useCallback(async () => {
    if (!isOwner) return setErr('소유자만 해지할 수 있습니다.');
    const signer = await getSigner();
    const erc721 = new Contract(nftAddress, TAKO_NFT_ABI, signer);
    return withTx(
      () => erc721.approve('0x0000000000000000000000000000000000000000', tid),
      '단건 승인 해지 완료'
    );
  }, [getSigner, isOwner, nftAddress, tid, withTx]);

  const toggleSetAll = useCallback(
    async (enable: boolean) => {
      if (!isOwner) return setErr('소유자만 설정할 수 있습니다.');
      if (!isAddress(spender)) return setErr('유효한 경매/마켓 컨트랙트 주소가 필요합니다.');
      if (spender.toLowerCase() === owner.toLowerCase()) return setErr('소유자 자신을 operator로 지정할 수 없습니다.');

      const signer = await getSigner();
      const erc721 = new Contract(nftAddress, TAKO_NFT_ABI, signer);
      return withTx(
        () => erc721.setApprovalForAll(spender, enable),
        enable ? '전역 승인(on) 완료' : '전역 승인(off) 완료'
      );
    },
    [getSigner, isOwner, spender, owner, nftAddress, withTx]
  );

  return (
    <div className="rounded-xl border border-[#30303a] bg-[#191924] p-4 text-sm text-gray-200">
      <div className="mb-3 flex items-center justify-between">
        <h3 className="text-base font-semibold">{L.title}</h3>
        <button
          onClick={() => readState()}
          disabled={loading}
          className="rounded-lg border border-gray-600 px-3 py-1 hover:bg-gray-700 disabled:opacity-60"
        >
          {L.refresh}
        </button>
      </div>

      <div className="grid gap-2 text-xs">
        <div>토큰 ID: <span className="font-mono">{tid.toString()}</span></div>
        <div>NFT: <span className="font-mono break-all">{nftAddress}</span></div>
        <div>경매 컨트랙트(Spender): <span className="font-mono break-all">{spender || '(미설정)'}</span></div>
        <div>내 계정: <span className="font-mono break-all">{account || '(지갑 미연결)'}</span></div>
        <div>소유자: <span className="font-mono break-all">{owner || '(조회 전)'}</span></div>
        <div>단건 승인 대상: <span className="font-mono break-all">{approved}</span></div>
        <div>전역 승인 상태: <span className={`font-semibold ${approvedForAll ? 'text-green-400' : 'text-gray-400'}`}>{approvedForAll ? 'ON' : 'OFF'}</span></div>
        <div>현재 상태: <span className={`font-semibold ${alreadyApproved ? 'text-green-400' : 'text-yellow-300'}`}>{alreadyApproved ? '승인됨' : '승인 필요'}</span></div>
      </div>

      <div className={`mt-4 flex flex-wrap gap-2 ${compact ? '-mt-1' : ''}`}>
        <button
          disabled={loading || !isOwner || alreadyApproved}
          onClick={() => void doApproveSingle()}
          className={`rounded-lg px-4 py-2 text-white ${alreadyApproved ? 'bg-gray-600' : 'bg-indigo-600 hover:bg-indigo-700'} disabled:opacity-60`}
          title={!isOwner ? '소유자만 승인 가능' : alreadyApproved ? '이미 승인됨' : ''}
        >
          {L.approveSingle}
        </button>

        <button
          disabled={loading || !isOwner}
          onClick={() => void doRevokeSingle()}
          className="rounded-lg bg-gray-700 px-4 py-2 text-white hover:bg-gray-600 disabled:opacity-60"
        >
          {L.revokeSingle}
        </button>

        <button
          disabled={loading || !isOwner || approvedForAll}
          onClick={() => void toggleSetAll(true)}
          className="rounded-lg bg-emerald-600 px-4 py-2 text-white hover:bg-emerald-700 disabled:opacity-60"
          title="모든 보유 NFT에 대해 경매 컨트랙트 전송 권한을 위임합니다."
        >
          {L.setAllOn}
        </button>

        <button
          disabled={loading || !isOwner || !approvedForAll}
          onClick={() => void toggleSetAll(false)}
          className="rounded-lg bg-rose-600 px-4 py-2 text-white hover:bg-rose-700 disabled:opacity-60"
        >
          {L.setAllOff}
        </button>
      </div>

      {loading && <p className="mt-3 text-xs text-blue-300">진행 중…</p>}
      {msg && <p className="mt-2 text-xs text-green-300">{msg}</p>}
      {err && <p className="mt-2 text-xs text-red-400">오류: {err}</p>}
    </div>
  );
}
