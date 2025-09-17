'use client'

import Image from "next/image"
import Link from "next/link"
import { Button } from "@/components/ui/button"

import { useState } from 'react'

export default function Mypage() {
  const tabs = [
    { id: 'myProfile', label: '기본정보' },
    { id: 'myBidAuction', label: '입찰중경매' },
    { id: 'mySellAuction', label: '판매경매' },
    { id: 'myReview', label: '리뷰' },
  ];
  const [status, setStatus] = useState(tabs[0].id);
  const activeIndex = tabs.findIndex(tab => tab.id === status);

  // NOTE: 2개의 탭으로 테스트하려면 아래와 같이 tabs 배열을 수정하세요.
  // const tabs = [
  //   { id: 'mySellAuction', label: '판매경매' },
  //   { id: 'myReview', label: '리뷰' },
  // ];

  return (
    <div className="flex flex-col gap-10">
      {/* 기본정보 */}
      <div className="flex gap-10 p-8 rounded-xl relative">
        <div className="flex-1 h-50 rounded-xl overflow-hidden relative z-1">
          <Image
            src="/no-image.jpg"
            alt="profile"
            fill
            style={{ objectFit: 'cover' }}
          />
        </div>
        <div className="flex-5 pt-8">
          <p className="mb-1 text-lg">nickname</p>
          <p className="text-sm text-[#D2D2D2]">
            TCG 10년차 전문가입니다. 많은 문의주세요
          </p>
        </div>
        <Button variant="outline" className="absolute top-10 right-10">
          <Link href="/mypage/edit">Edit Profile</Link>
        </Button>
        <div className="pl-8 flex gap-10 w-full bg-[#353444] absolute bottom-0 left-0 rounded-bl-xl rounded-br-xl overflow-hidden">
          <div className="flex-1"></div>
          <div className="flex-5 relative">
            <ul className={`grid grid-cols-${tabs.length}`}>
              {tabs.map(tab => (
                <li
                  key={tab.id}
                  className="text-center py-4 hover:bg-gray-600 cursor-pointer"
                  onClick={() => {
                    setStatus(tab.id);
                  }}
                >
                  {tab.label}
                </li>
              ))}
            </ul>
            {activeIndex !== -1 && (
              <div
                className="absolute -bottom-1.5 transition-all duration-300"
                style={{
                  left: `calc(${
                    activeIndex * (100 / tabs.length) + 50 / tabs.length
                  }% - 15px)`,
                }}
              >
                <Image src="/icon/current-arrow.svg" alt="current" width={30} height={14} />
              </div>
            )}
          </div>
        </div>
      </div>

      {status === 'myProfile' && (
        <div>
          <h2>기본정보</h2>
          <div className="flex-1 p-8 border-1 border-[#353535] bg-[#191924] rounded-xl flex justify-between">
            <div className="flex flex-col justify-between">
              <h3>내 지갑</h3>
              <div className="flex flex-col gap-1">
                <p className="text-[#D2D2D2] mb-1">보유자산</p>
                <div className="flex justify-between gap-6">
                  <p className="text-2xl text-[#A4B2FF] font-semibold">
                    46,500,888
                  </p>
                  <p className="text-[#D2D2D2]">KRW</p>
                </div>
                <div className="flex justify-between gap-6">
                  <p className="text-2xl text-[#A4B2FF] font-semibold">3.0000</p>
                  <p className="text-[#D2D2D2]">TKC</p>
                </div>
              </div>
            </div>
            <ul className="flex flex-col gap-3">
              <li className="py-3 px-12 border-1 border-[#353535]">코인교환</li>
              <li className="py-3 px-12 border-1 border-[#353535]">충전하기</li>
              <li className="py-3 px-12 border-1 border-[#353535]">송금하기</li>
            </ul>
          </div>
        </div>
      )}
      {status === 'myBidAuction' && (
        <div>
          <h2>입찰중경매</h2>
        </div>
      )}
      {status === 'mySellAuction' && (
        <div>
          <h2>판매경매</h2>
        </div>
      )}
      {status === 'myReview' && (
        <div>
          <h2>리뷰</h2>
        </div>
      )}
    </div>
  );
}