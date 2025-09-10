'use client';

import { useState } from 'react';
import BuyAuction from '@/components/sections/auction/BuyAuction';

export default function BuyAuctionPage() {
  const [activeTab, setActiveTab] = useState<'ongoing' | 'ended'>('ongoing');

  return (
    <div>
      <h2>입찰 경매 조회</h2>

      {/* 구분 탭 */}
      <div className="border-b border-[#d9d9d9] flex relative py-4 mt-3">
        {/* 경매 중 */}
        <button
          onClick={() => setActiveTab('ongoing')}
          className="flex-1 flex flex-col gap-1 items-center justify-center cursor-pointer"
        >
          <div className={`text-xl font-semibold ${
            activeTab === 'ongoing' ? 'text-[#F2B90C]' : ''
          }`}>3</div>
          <div className="text-white text-md">경매 중</div>
        </button>
        {/* 종료 */}
        <button
          onClick={() => setActiveTab('ended')}
          className="flex-1 flex flex-col gap-1 items-center justify-center cursor-pointer"
        >
          <div className={`text-xl font-semibold ${
            activeTab === 'ended' ? 'text-[#F2B90C]' : ''
          }`}>2</div>
          <div className="text-white text-md">종료</div>
        </button>
        
        {/* 활성 탭 표시 바 */}
        <div 
          className={`absolute bottom-0 w-[50%] h-[2px] bg-[#F2B90C] transition-all duration-300 ease-in-out z-1 ${
            activeTab === 'ongoing' ? 'left-0' : 'left-[50%] w-[50%]'
          }`}
        />
      </div>

      {/* 경매 목록 테이블 */}
      <div className='mt-6'>
        <BuyAuction />
      </div>
    </div>
  );
}