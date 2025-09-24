'use client';

import React, { useState, useCallback } from 'react';
import useWallet from '@/hooks/useWallet';

const WalletProfile: React.FC = () => {
  const {
    walletAddress,
    chainName,
    balance,
    error,
    loading,
    needsMetaMask,
    connectWallet,
    disconnect,
    switchNetwork,
    installMetaMask,
    reloadAndAutoConnect,   // 새로 추가된 훅 함수
    isInstalling,           // 설치 감지 중 상태
  } = useWallet();

  const [target, setTarget] = useState<'mainnet' | 'sepolia'>('sepolia');
  const short = (addr: string) => `${addr.slice(0, 6)}...${addr.slice(-4)}`;

  const onReloadAndAuto = useCallback(() => {
    reloadAndAutoConnect(); // 로컬 플래그 심고 새로고침
  }, [reloadAndAutoConnect]);

  return (
    <div className="flex-1 p-8 border border-[#353535] bg-[#191924] rounded-xl flex justify-between">
      <div className="flex flex-col gap-4">
        <h3 className="text-white text-xl font-semibold">내 지갑 정보</h3>

        {needsMetaMask && !walletAddress ? (
          <div className="flex flex-col gap-3">
            <p className="text-[#D2D2D2]">
              메타마스크가 설치되어 있지 않습니다.
            </p>
            <div className="flex gap-2 flex-wrap">
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
                {isInstalling
                  ? '설치 감지 중...'
                  : loading
                  ? '확인 중...'
                  : '설치 완료 — 자동 연결'}
              </button>

              <button
                onClick={onReloadAndAuto}
                className="bg-neutral-700 hover:bg-neutral-600 text-white font-semibold py-2 px-4 rounded-lg"
                title="브라우저가 확장 프로그램 주입을 위해 새로고침이 필요할 수 있어요."
              >
                새로고침 후 자동 연결
              </button>
            </div>
            <p className="text-xs text-[#9aa0a6]">
              설치 후 이 탭으로 돌아오면 자동으로 연결을 시도합니다. 새로고침이 필요하면
              위 버튼을 눌러 주세요.
            </p>
          </div>
        ) : walletAddress ? (
          <div>
            <div className="flex flex-col gap-2">
              <div className="flex gap-3">
                <span className="text-[#D2D2D2]">지갑</span>
                <span className="text-2xl text-[#A4B2FF] font-semibold">
                  {short(walletAddress)}
                </span>
              </div>
              <div className="flex gap-3">
                <span className="text-[#D2D2D2]">네트워크</span>
                <span className="text-lg text-yellow-400 font-medium">
                  {chainName || '-'}
                </span>
              </div>
              <div className="flex gap-3">
                <span className="text-[#D2D2D2]">잔액</span>
                <span className="text-lg text-green-400 font-medium">
                  {balance || '-'}
                </span>
              </div>
            </div>

            <div className="flex items-center gap-2 mt-4 flex-wrap">
              <select
                className="bg-[#101018] text-white border border-[#353535] rounded-lg px-3 py-2"
                value={target}
                onChange={(e) =>
                  setTarget(e.target.value as 'mainnet' | 'sepolia')
                }
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
                // 백엔드 delete 기능 미구현
                // onClick={disconnect}
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
