'use client'

import Image from "next/image"
import Link from "next/link"
import { Button } from "@/components/ui/button"

import { useState } from 'react'
import WalletProfile from "@/components/wallet/WalletProfile" 
// import ClaimButton from "@/components/nft/ClaimButton"
import HomeBidAuctions from "@/components/mypage/HomeBidAuctions"

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
      <div className="flex gap-10 p-8 rounded-xl relative bg-gradient-to-b from-[#073A4B] to-[#3B80FF]">
        <div className="flex-2 aspect-square rounded-xl overflow-hidden relative z-1">
          <Image
            src="/basic-profile.png"
            alt="profile"
            fill
            style={{ objectFit: 'cover' }}
          />
        </div>
        <div className="flex-7 pt-8">
          <p className="mb-1 text-lg">nickname</p>
          <p className="text-sm text-[#D2D2D2]">
            TCG 10년차 전문가입니다. 많은 문의주세요
          </p>
        </div>
        <Button variant="outline" className="absolute top-10 right-10">
          <Link href="/mypage/edit">Edit Profile</Link>
        </Button>
        <div className="pl-8 flex gap-10 w-full bg-[#3D3D4D] absolute bottom-0 left-0 rounded-bl-xl rounded-br-xl overflow-hidden">
          <div className="flex-2"></div>
          <div className="flex-7 relative">
            <ul className={`grid grid-cols-${tabs.length}`}>
              {tabs.map(tab => (
                <li
                  key={tab.id}
                  className={`text-center py-4 cursor-pointer hover:text-white ${
                    status === tab.id ? "text-white font-medium" : "text-[#a5a5a5]"
                  }`}
                  onClick={() => { setStatus(tab.id); }}
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
          <WalletProfile />
        </div>
      )}
      {status === 'myBidAuction' && (
        <div>
          <HomeBidAuctions />
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