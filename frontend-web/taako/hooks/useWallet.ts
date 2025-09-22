'use client';

import { useState, useCallback, useEffect } from 'react';
import { BrowserProvider, formatEther } from 'ethers';
import { MetaMaskInpageProvider } from '@metamask/providers';
import { sendWalletAddress } from '@/lib/bc/wallet';

// 지원 네트워크 정의 (원하면 더 추가)
const NETWORKS = {
  mainnet: {
    chainIdHex: '0x1',
    chainName: 'Ethereum Mainnet',
    rpcUrls: ['https://rpc.ankr.com/eth'],
    blockExplorerUrls: ['https://etherscan.io'],
    nativeCurrency: { name: 'Ether', symbol: 'ETH', decimals: 18 },
  },
  sepolia: {
    chainIdHex: '0xAA36A7', // 11155111
    chainName: 'Sepolia Test Network',
    rpcUrls: ['https://rpc.sepolia.org'],
    blockExplorerUrls: ['https://sepolia.etherscan.io'],
    nativeCurrency: { name: 'Sepolia ETH', symbol: 'SEP', decimals: 18 },
  },
} as const;
type NetworkKey = keyof typeof NETWORKS;

declare global {
  interface Window {
    ethereum?: MetaMaskInpageProvider & { isMetaMask?: boolean; providers?: MetaMaskInpageProvider[] };
  }
}

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
  const [walletAddress, setWalletAddress] = useState('');
  const [chainName, setChainName] = useState('');
  const [balance, setBalance] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const getMetaMask = (): MetaMaskInpageProvider | undefined => {
    const eth = typeof window !== 'undefined' ? window.ethereum : undefined;
    if (!eth) return undefined;
    const anyEth = eth as any;
    if (Array.isArray(anyEth.providers)) {
      return anyEth.providers.find((p: any) => p?.isMetaMask) as MetaMaskInpageProvider | undefined;
    }
    return eth as MetaMaskInpageProvider;
  };

  const prettyEth = (wei: bigint) => {
    const eth = parseFloat(formatEther(wei));
    return Number.isFinite(eth) ? eth.toFixed(4) : '0.0000';
  };

  const fetchChainAndBalance = useCallback(async (provider: BrowserProvider, address: string) => {
    try {
      const network = await provider.getNetwork();
      const bal = await provider.getBalance(address);
      // network.name은 mainnet에서 'homestead'일 수 있음 → 보기 좋은 이름으로 치환
      const friendly =
        network.chainId === 1n ? 'mainnet' :
        network.chainId === 11155111n ? 'sepolia' :
        (network.name || `chain(${network.chainId})`);
      setChainName(friendly);
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
      if (typeof window === 'undefined') throw new Error('클라이언트에서만 실행됩니다.');
      const mm = getMetaMask();
      if (!mm) throw new Error('메타마스크가 설치되어 있지 않습니다.');

      const provider = new BrowserProvider(mm);
      await provider.send('eth_requestAccounts', []);
      const signer = await provider.getSigner();
      const addr = await signer.getAddress();

      if (walletAddress && addr.toLowerCase() === walletAddress.toLowerCase()) {
        setError('이미 사용 중인 지갑입니다.');
        await fetchChainAndBalance(provider, addr);
        return;
      }

      setWalletAddress(addr);
      await upsertAddress(addr);
      await fetchChainAndBalance(provider, addr);
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

  // ✅ 네트워크 전환
  const switchNetwork = useCallback(async (key: NetworkKey) => {
    setError('');
    try {
      const mm = getMetaMask();
      if (!mm) throw new Error('메타마스크가 설치되어 있지 않습니다.');
      const target = NETWORKS[key];
      // 1) 스위치 시도
      await (mm as any).request({
        method: 'wallet_switchEthereumChain',
        params: [{ chainId: target.chainIdHex }],
      });
    } catch (e: any) {
      // 4902: Unrecognized chain → 체인 추가 후 재시도
      if (e?.code === 4902) {
        const target = NETWORKS[key];
        await (window.ethereum as any).request({
          method: 'wallet_addEthereumChain',
          params: [{
            chainId: target.chainIdHex,
            chainName: target.chainName,
            rpcUrls: target.rpcUrls,
            blockExplorerUrls: target.blockExplorerUrls,
            nativeCurrency: target.nativeCurrency,
          }],
        });
        await (window.ethereum as any).request({
          method: 'wallet_switchEthereumChain',
          params: [{ chainId: target.chainIdHex }],
        });
      } else {
        // 유저가 거절(4001) 등
        setError(e?.message || '네트워크 전환 실패');
        throw e;
      }
    }

    // 전환 후 최신 정보 갱신
    try {
      if (!walletAddress) return;
      const provider = new BrowserProvider(window.ethereum!);
      await fetchChainAndBalance(provider, walletAddress);
    } catch (e) {
      console.error('[wallet] switch refresh error', e);
    }
  }, [walletAddress, fetchChainAndBalance]);

  useEffect(() => {
    const mm = getMetaMask();
    if (!mm) return;

    const onAccountsChanged = async (...args: unknown[]) => {
      const accounts = Array.isArray(args[0]) ? (args[0] as string[]) : [];
      const next = accounts[0] ?? '';
      if (!next) {
        disconnect();
        return;
      }
      if (walletAddress && next.toLowerCase() === walletAddress.toLowerCase()) {
        try {
          const provider = new BrowserProvider(mm);
          await fetchChainAndBalance(provider, next);
        } catch {}
        return;
      }
      setWalletAddress(next);
      try {
        const provider = new BrowserProvider(mm);
        await upsertAddress(next);
        await fetchChainAndBalance(provider, next);
      } catch {}
    };

    const onChainChanged = async (..._args: unknown[]) => {
      if (!walletAddress) return;
      try {
        const provider = new BrowserProvider(mm);
        await fetchChainAndBalance(provider, walletAddress);
      } catch (e) {
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
        const provider = new BrowserProvider(mm);
        const accounts = await provider.send('eth_accounts', []);
        if (accounts?.[0]) {
          setWalletAddress(accounts[0]);
          await fetchChainAndBalance(provider, accounts[0]);
        }
      } catch {}
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
