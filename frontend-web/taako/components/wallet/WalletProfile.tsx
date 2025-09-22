'use client';

import React, { useState } from 'react';
import useWallet from '@/hooks/useWallet';

const WalletProfile: React.FC = () => {
  const { walletAddress, chainName, balance, error, loading, connectWallet, disconnect, switchNetwork, needsMetaMask, installMetaMask } = useWallet();
  const [target, setTarget] = useState<'mainnet' | 'sepolia'>('sepolia');

  const short = (addr: string) => `${addr.slice(0, 6)}...${addr.slice(-4)}`;

  return (
    <div className="flex-1 p-8 border border-[#353535] bg-[#191924] rounded-xl flex justify-between">
      <div className="flex flex-col gap-4">
        <h3 className="text-white text-xl font-semibold">내 지갑 정보</h3>

        {needsMetaMask && !walletAddress ? (
          <div className="flex flex-col gap-3">
            <p className="text-[#D2D2D2]">메타마스크가 설치되어 있지 않습니다.</p>
            <div className="flex gap-2">
              <button
                onClick={installMetaMask}
                className="bg-orange-500 hover:bg-orange-600 text-white font-semibold py-2 px-4 rounded-lg"
              >
                메타마스크 설치하기
              </button>
              <button
                onClick={connectWallet}
                disabled={loading}
                className="bg-blue-500 hover:bg-blue-600 disabled:bg-blue-400 text-white font-semibold py-2 px-4 rounded-lg"
              >
                {loading ? '확인 중...' : '설치 후 연결 확인'}
              </button>
            </div>
          </div>
        ) : walletAddress ? (
          <div>
            <div className="flex flex-col gap-2">
              <div className="flex gap-3">
                <span className="text-[#D2D2D2]">지갑</span>
                <span className="text-2xl text-[#A4B2FF] font-semibold">{short(walletAddress)}</span>
              </div>
              <div className="flex gap-3">
                <span className="text-[#D2D2D2]">네트워크</span>
                <span className="text-lg text-yellow-400 font-medium">{chainName || '-'}</span>
              </div>
              <div className="flex gap-3">
                <span className="text-[#D2D2D2]">잔액</span>
                <span className="text-lg text-green-400 font-medium">{balance || '-'}</span>
              </div>
            </div>

            <div className="flex items-center gap-2 mt-4">
              <select
                className="bg-[#101018] text-white border border-[#353535] rounded-lg px-3 py-2"
                value={target}
                onChange={(e) => setTarget(e.target.value as 'mainnet' | 'sepolia')}
                >
                <option value="sepolia">Sepolia</option>
                <option value="mainnet">Mainnet</option>
              </select>
              <button
                onClick={() => switchNetwork(target)}
                disabled={loading}
                className="bg-purple-600 hover:bg-purple-500 disabled:bg-purple-400 text-white font-semibold py-2 px-4 rounded-lg"
                >
                {loading ? '전환 중...' : '네트워크 전환'}
              </button>

              <button
                className="bg-neutral-700 hover:bg-neutral-600 text-white font-semibold py-2 px-4 rounded-lg"
                >
                연결 해제
              </button>
            </div>
          </div>
        ) : (
          <button
            onClick={connectWallet}
            disabled={loading}
            className="bg-blue-500 hover:bg-blue-600 disabled:bg-blue-400 text-white font-bold py-3 px-6 rounded-lg"
          >
            {loading ? '연결중...' : '지갑 연동하기'}
          </button>
        )}

        {error && <p className="text-red-500">{error}</p>}
      </div>
    </div>
  );
};

export default WalletProfile;
