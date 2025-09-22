'use client';

import { useState, useCallback, useEffect } from 'react';
import { formatEther } from 'ethers';
import { sendWalletAddress } from '@/lib/bc/wallet';
import {
  getBrowserProvider,
  getMetaMaskProvider,
  friendlyChainName,
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
  connectWallet: () => Promise<void>;
  disconnect: () => void;
  switchNetwork: (key: NetworkKey) => Promise<void>; // ✅ 추가
}

const useWallet = (): UseWalletReturn => {
  const [walletAddress, setWalletAddress] = useState<string>('');
  const [chainName, setChainName] = useState<string>('');
  const [balance, setBalance] = useState<string>('');
  const [error, setError] = useState<string>('');
  const [loading, setLoading] = useState(false);

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
    setLoading(true);
    try {
      const provider = getBrowserProvider();
      if (!provider) throw new Error('메타마스크가 설치되어 있지 않습니다.');
      await provider.send('eth_requestAccounts', []);
      const signer = await provider.getSigner();
      const addr = await signer.getAddress();

      if (walletAddress && addr.toLowerCase() === walletAddress.toLowerCase()) {
        // 같은 지갑이면 리프레시만
        await fetchChainAndBalance(addr);
        return;
      }

      setWalletAddress(addr);
      await upsertAddress(addr);
      await fetchChainAndBalance(addr);
    } catch (err: any) {
      if (err?.code === 4001) setError('사용자가 연결을 거절했습니다.');
      else setError(err?.message ?? '지갑 연결 중 오류가 발생했습니다.');
      setWalletAddress('');
      setChainName('');
      setBalance('');
      console.error('[wallet] connect error:', err);
    } finally {
      setLoading(false);
    }
  }, [walletAddress, upsertAddress, fetchChainAndBalance]);

  const disconnect = useCallback(() => {
    setWalletAddress('');
    setChainName('');
    setBalance('');
    setError('');
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

  // 이벤트 바인딩
  useEffect(() => {
    const mm = getMetaMaskProvider() as MetaMaskInpageProvider | undefined;
    if (!mm) return;

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

  return { walletAddress, chainName, balance, error, loading, connectWallet, disconnect, switchNetwork };
};

export default useWallet;
