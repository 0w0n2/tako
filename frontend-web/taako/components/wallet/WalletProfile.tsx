'use client';

import React from 'react';
import useWallet from '@/hooks/useWallet';

const WalletProfile: React.FC = () => {
  const { walletAddress, error, loading, connectWallet } = useWallet();

  const short = (addr: string) => `${addr.slice(0, 6)}...${addr.slice(-4)}`;

  return (
    <div className="flex-1 p-8 border border-[#353535] bg-[#191924] rounded-xl flex justify-between">
      <div className="flex flex-col gap-4">
        <h3 className="text-white text-xl font-semibold">메타마스크 지갑</h3>

        {walletAddress ? (
          <>
            <div className="flex items-center gap-4">
              <span className="text-[#D2D2D2]">지갑 주소</span>
              <span className="text-2xl text-[#A4B2FF] font-semibold">
                {short(walletAddress)}
              </span>
            </div>

            <div className="flex gap-2">
              <button
                className="bg-neutral-700 hover:bg-neutral-600 text-white font-semibold py-2 px-4 rounded-lg transition-colors"
              >
                연결 해제
              </button>
              <button
                onClick={connectWallet}
                disabled={loading}
                className="bg-blue-500 hover:bg-blue-600 disabled:bg-blue-400 text-white font-bold py-3 px-6 rounded-lg transition-colors"
              >
                {loading ? '연결중...' : '지갑 다시 연결하기'}
              </button>
            </div>
          </>
        ) : (
          <button
            onClick={connectWallet}
            disabled={loading}
            className="bg-blue-500 hover:bg-blue-600 disabled:bg-blue-400 text-white font-bold py-3 px-6 rounded-lg transition-colors"
          >
            {loading ? '연결중...' : '지갑 연동하기'}
          </button>
        )}

        {error && <p className="text-red-500">{error}</p>}
      </div>

      {/* 필요한 경우 오른쪽 액션 메뉴 */}
      <ul className="flex flex-col gap-3">
        <li className="py-3 px-12 border border-[#353535] text-white/80">코인교환</li>
        <li className="py-3 px-12 border border-[#353535] text-white/80">충전하기</li>
        <li className="py-3 px-12 border border-[#353535] text-white/80">송금하기</li>
      </ul>
    </div>
  );
};

export default WalletProfile;
