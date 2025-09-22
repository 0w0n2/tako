'use client';

import { useState, useCallback, useEffect } from 'react';
import { formatEther } from 'ethers';
import { sendWalletAddress } from '@/lib/bc/wallet';
import {
  getBrowserProvider,
  getMetaMaskProvider,
  friendlyChainName,
  getMetaMaskInstallUrl,
  switchNetwork as ethSwitchNetwork, // ← 유틸 가져오기
  type NetworkKey,                    // ← 타입도 가져오기
} from '@/lib/ethereum';
import type { MetaMaskInpageProvider } from '@metamask/providers';

interface UseWalletReturn {
  walletAddress: string;
  chainName: string;
  balance: string;
  error: string;
  loading: boolean;
  needsMetaMask: boolean;
  connectWallet: () => Promise<void>;
  disconnect: () => void;
  switchNetwork: (key: NetworkKey) => Promise<void>;
  installMetaMask: () => void;
}

const useWallet = (): UseWalletReturn => {
  const [walletAddress, setWalletAddress] = useState<string>('');
  const [chainName, setChainName] = useState<string>('');
  const [balance, setBalance] = useState<string>('');
  const [error, setError] = useState<string>('');
  const [loading, setLoading] = useState(false);
  const [needsMetaMask, setNeedsMetaMask] = useState(false);

  const prettyEth = (wei: bigint) => {
    const eth = parseFloat(formatEther(wei));
    return Number.isFinite(eth) ? eth.toFixed(4) : '0.0000';
    // 필요하면 Intl.NumberFormat으로 포맷 업그레이드 가능
  };

  const fetchChainAndBalance = useCallback(async (address: string) => {
    const provider = getBrowserProvider();
    if (!provider) return;
    try {
      const net = await provider.getNetwork();
      const bal = await provider.getBalance(address);
      setChainName(friendlyChainName(net.chainId, net.name));
      setBalance(`${prettyEth(bal)} ETH`);
    } catch (e) {
      console.error('[wallet] fetchChainAndBalance error', e);
      setChainName('');
      setBalance('');
    }
  }, []);

  const upsertAddress = useCallback(async (addr: string) => {
    try {
      await sendWalletAddress(addr);
    } catch (e: any) {
      const msg = e?.response?.data?.message || e?.message || '지갑 주소 저장 실패';
      setError(msg);
      console.error('[wallet] upsert error:', e);
      throw e;
    }
  }, []);

  const connectWallet = useCallback(async () => {
    setError('');
    setNeedsMetaMask(false);
    setLoading(true);
    try {
      let provider = getBrowserProvider();
      if (!provider) {
        // ⏳ 설치 체크 모드: 짧게 대기
        setNeedsMetaMask(true);
        const found = await waitForMetaMask(15000, 500); // 15초 폴링
        if (!found) {
          setError('메타마스크 설치 후 새로고침이 필요할 수 있습니다.');
          return;
        }
        // 주입되었으면 이어서 진행
        provider = getBrowserProvider();
        if (!provider) {
          setError('메타마스크 감지에 실패했습니다. 새로고침 후 다시 시도해주세요.');
          return;
        }
        setNeedsMetaMask(false);
      }

      await provider.send('eth_requestAccounts', []);
      const signer = await provider.getSigner();
      const addr = await signer.getAddress();

      if (walletAddress && addr.toLowerCase() === walletAddress.toLowerCase()) {
        await fetchChainAndBalance(addr); // 같은 지갑이면 새로고침만
        return;
      }

      setWalletAddress(addr);
      await upsertAddress(addr);
      await fetchChainAndBalance(addr);
    } catch (err: any) {
      if (err?.code === 4001) setError('사용자가 연결을 거절했습니다.');
      else setError(err?.message ?? '지갑 연결 중 오류가 발생했습니다.');
      if (!needsMetaMask) {
        setWalletAddress('');
        setChainName('');
        setBalance('');
      }
      console.error('[wallet] connect error:', err);
    } finally {
      setLoading(false);
    }
  }, [walletAddress, upsertAddress, fetchChainAndBalance, needsMetaMask]);

  const disconnect = useCallback(() => {
    setWalletAddress('');
    setChainName('');
    setBalance('');
    setError('');
  }, []);

  const installMetaMask = useCallback(() => {
    const url = getMetaMaskInstallUrl();
    // 새 탭으로 안전하게 열기
    if (typeof window !== 'undefined') {
      window.open(url, '_blank', 'noopener,noreferrer');
    }
  }, []);

  // ✅ 네트워크 전환: 훅에서 노출
  const switchNetwork = useCallback(async (key: NetworkKey) => {
    setError('');
    setLoading(true);
    try {
      await ethSwitchNetwork(key);        // 네트워크 전환(추가 포함)
      if (walletAddress) {
        await fetchChainAndBalance(walletAddress); // 전환 후 최신화
      }
    } catch (e: any) {
      if (e?.code === 4001) setError('사용자가 네트워크 전환을 거절했습니다.');
      else setError(e?.message || '네트워크 전환 실패');
      console.error('[wallet] switchNetwork error', e);
      throw e;
    } finally {
      setLoading(false);
    }
  }, [walletAddress, fetchChainAndBalance]);

  // MetaMask 설치 감지: interval 동안 window.ethereum 등장 대기
  const waitForMetaMask = (timeoutMs = 15000, intervalMs = 500): Promise<boolean> =>
    new Promise((resolve) => {
      if (typeof window === 'undefined') return resolve(false);

      // 이미 주입되어 있으면 즉시 true
      if ((window as any).ethereum) return resolve(true);

      let elapsed = 0;
      const iv = window.setInterval(() => {
        elapsed += intervalMs;
        if ((window as any).ethereum) {
          window.clearInterval(iv);
          resolve(true);
        } else if (elapsed >= timeoutMs) {
          window.clearInterval(iv);
          resolve(false);
        }
      }, intervalMs);

      // 탭 포커스가 돌아올 때도 한 번 더 확인
      const onVisible = () => {
        if ((window as any).ethereum) {
          document.removeEventListener('visibilitychange', onVisible);
          window.clearInterval(iv);
          resolve(true);
        }
      };
      document.addEventListener('visibilitychange', onVisible, { once: true });
    });


  // 이벤트 바인딩
  useEffect(() => {
    const mm = getMetaMaskProvider() as MetaMaskInpageProvider | undefined;
    if (!mm) {
      setNeedsMetaMask(true);
      return;
    }

    const onAccountsChanged = async (...args: unknown[]) => {
      const accounts = Array.isArray(args[0]) ? (args[0] as string[]) : [];
      const next = accounts[0] ?? '';

      if (!next) {
        disconnect();
        return;
      }
      if (walletAddress && next.toLowerCase() === walletAddress.toLowerCase()) {
        try { await fetchChainAndBalance(next); } catch {}
        return;
      }
      setWalletAddress(next);
      try {
        await upsertAddress(next);
        await fetchChainAndBalance(next);
      } catch {}
    };

    const onChainChanged = async (..._args: unknown[]) => {
      if (!walletAddress) return;
      try { await fetchChainAndBalance(walletAddress); } catch (e) {
        console.error('[wallet] chainChanged refetch error', e);
      }
    };

    const onDisconnect = (..._args: unknown[]) => {
      disconnect();
    };

    mm.on?.('accountsChanged', onAccountsChanged as (...args: unknown[]) => void);
    mm.on?.('chainChanged', onChainChanged as (...args: unknown[]) => void);
    mm.on?.('disconnect', onDisconnect as (...args: unknown[]) => void);

    (async () => {
      try {
        const provider = getBrowserProvider();
        if (!provider) return;
        const accounts = await provider.send('eth_accounts', []);
        if (accounts?.[0]) {
          setNeedsMetaMask(false);
          setWalletAddress(accounts[0]);
          await fetchChainAndBalance(accounts[0]);
        }
      } catch {/* noop */}
    })();

    return () => {
      mm.removeListener?.('accountsChanged', onAccountsChanged as (...args: unknown[]) => void);
      mm.removeListener?.('chainChanged', onChainChanged as (...args: unknown[]) => void);
      mm.removeListener?.('disconnect', onDisconnect as (...args: unknown[]) => void);
    };
  }, [walletAddress, fetchChainAndBalance, upsertAddress, disconnect]);

  return { walletAddress, chainName, balance, error, loading, connectWallet, disconnect, switchNetwork, needsMetaMask, installMetaMask};
};

export default useWallet;
