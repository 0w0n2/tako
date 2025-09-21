'use client';

import { useState, useCallback, useEffect } from 'react';
import { BrowserProvider } from 'ethers';
import { MetaMaskInpageProvider } from '@metamask/providers';
import { sendWalletAddress } from '@/lib/bc/wallet';

// window.ethereum 타입 선언
declare global {
  interface Window {
    ethereum?: MetaMaskInpageProvider & {
      isMetaMask?: boolean;
      providers?: MetaMaskInpageProvider[];
    };
  }
}

interface UseWalletReturn {
  walletAddress: string;
  error: string;
  loading: boolean;
  connectWallet: () => Promise<void>;
  disconnect: () => void;
}

const useWallet = (): UseWalletReturn => {
  const [walletAddress, setWalletAddress] = useState<string>('');
  const [error, setError] = useState<string>('');
  const [loading, setLoading] = useState(false);

  /** 여러 지갑이 깔린 경우 메타마스크 provider 선택 */
  const getMetaMask = (): MetaMaskInpageProvider | undefined => {
    const eth = typeof window !== 'undefined' ? window.ethereum : undefined;
    if (!eth) return undefined;
    if (Array.isArray(eth.providers)) {
      return eth.providers.find((p: any) => (p as any).isMetaMask) as MetaMaskInpageProvider | undefined;
    }
    return eth as MetaMaskInpageProvider;
  };

  /** 서버로 주소 업서트 (에러 메시지 일원화) */
  const upsertAddress = useCallback(async (addr: string) => {
    try {
      await sendWalletAddress(addr); // 백엔드는 POST=UPSERT
    } catch (e: any) {
      const msg = e?.response?.data?.message || e?.message || '지갑 주소 저장 실패';
      setError(msg);
      // 콘솔은 디버깅용
      console.error('[wallet] upsert error:', e);
      throw e;
    }
  }, []);

  /** 지갑 연결 */
  const connectWallet = useCallback(async () => {
    setError('');
    setLoading(true);
    try {
      if (typeof window === 'undefined') throw new Error('클라이언트에서만 실행됩니다.');
      const mm = getMetaMask();
      if (!mm) throw new Error('메타마스크가 설치되어 있지 않습니다.');

      const provider = new BrowserProvider(mm);
      await provider.send('eth_requestAccounts', []);
      const signer = await provider.getSigner();
      const addr = await signer.getAddress();

      // 현재 state와 같으면 “이미 사용 중” 처리
      if (addr.toLowerCase() === walletAddress.toLowerCase()) {
        setError('이미 사용 중인 지갑입니다.');
        return;
      }

      // 상태 반영 + 서버 업서트
      setWalletAddress(addr);
      await upsertAddress(addr);
    } catch (err: any) {
      if (err?.code === 4001) setError('사용자가 연결을 거절했습니다.');
      else setError(err?.message ?? '지갑 연결 중 오류가 발생했습니다.');
      setWalletAddress('');
      console.error('[wallet] connect error:', err);
    } finally {
      setLoading(false);
    }
  }, [walletAddress, upsertAddress]);

  /** 로컬만 끊기 (서버 주소 해제 API가 없다면 상태만 초기화) */
  const disconnect = useCallback(() => {
    setWalletAddress('');
    setError('');
  }, []);

  /** 계정/체인 이벤트 바인딩: 계정이 바뀌면 서버에 업서트 */
  useEffect(() => {
    const mm = getMetaMask();
    if (!mm) return;

    const onAccountsChanged = async (...args: unknown[]) => {
      const accounts = Array.isArray(args[0]) ? (args[0] as string[]) : [];
      const next = accounts[0] ?? '';

      // 주소가 비면 상태만 비움 (서버 해제는 정책에 따라 별도 처리)
      if (!next) {
        setWalletAddress('');
        return;
      }

      // 동일 주소면 아무 것도 안 함
      if (walletAddress && next.toLowerCase() === walletAddress.toLowerCase()) return;

      // 변경: 상태 갱신 후 업서트
      setWalletAddress(next);
      try {
        await upsertAddress(next);
      } catch {
        /* 에러는 upsertAddress에서 처리됨 */
      }
    };

    const onChainChanged = (..._args: unknown[]) => {
      // 체인 변경 시 dapp 상태 초기화가 가장 안전
      window.location.reload();
    };

    const onDisconnect = (..._args: unknown[]) => {
      disconnect();
    };

    // 리스너 등록
    mm.on?.('accountsChanged', onAccountsChanged as (...args: unknown[]) => void);
    mm.on?.('chainChanged', onChainChanged as (...args: unknown[]) => void);
    mm.on?.('disconnect', onDisconnect as (...args: unknown[]) => void);

    // 페이지 로드 시 이미 연결된 계정이 있으면 상태만 반영 (불필요한 업서트 방지)
    (async () => {
      try {
        const provider = new BrowserProvider(mm);
        const accounts = await provider.send('eth_accounts', []);
        if (accounts?.[0]) setWalletAddress(accounts[0]);
      } catch {/* noop */}
    })();

    return () => {
      mm.removeListener?.('accountsChanged', onAccountsChanged as (...args: unknown[]) => void);
      mm.removeListener?.('chainChanged', onChainChanged as (...args: unknown[]) => void);
      mm.removeListener?.('disconnect', onDisconnect as (...args: unknown[]) => void);
    };
  }, [walletAddress, upsertAddress, disconnect]);

  return { walletAddress, error, loading, connectWallet, disconnect };
};

export default useWallet;
