'use client';

import { useState } from 'react';

export default function CreateReviewPage(){
    const [activeTab, setActiveTab] = useState<'ongoing' | 'ended'>('ongoing');
    return(
        <div>
            <h2>리뷰쓰기</h2>

            <div className="border-b border-[#a5a5a5] flex relative py-2 mt-3">
                <button
                onClick={() => setActiveTab('ongoing')}
                className="flex-1 flex gap-2 items-center justify-center cursor-pointer"
                >
                <div className={`${activeTab === 'ongoing' ? 'text-white font-medium' : 'font-light text-[#a5a5a5]'}`}>리뷰 쓰기</div>
                <div className={`text-lg mb-0.5 ${
                    activeTab === 'ongoing' ? 'font-medium' : 'font-light text-[#a5a5a5]'
                }`}>0</div>
                </button>
                <button
                onClick={() => setActiveTab('ended')}
                className="flex-1 flex gap-2 items-center justify-center cursor-pointer"
                >
                <div className={`${activeTab === 'ended' ? 'text-white font-medium' : 'font-light text-[#a5a5a5]'}`}>내 리뷰</div>
                <div className={`text-lg mb-0.5 ${
                    activeTab === 'ended' ? 'font-medium' : 'font-light text-[#a5a5a5]'
                }`}>0</div>
                </button>
                
                {/* 활성 탭 표시 바 */}
                <div 
                className={`absolute bottom-0 w-[50%] h-[1px] bg-white transition-all duration-300 ease-in-out z-1 ${
                    activeTab === 'ongoing' ? 'left-0' : 'left-[50%] w-[50%]'
                }`}
                />
            </div>
        </div>
    )
}