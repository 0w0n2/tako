// 마이페이지 전용 사이드 메뉴
"use client"

import Link from "next/link"
import { useState } from "react"
import { usePathname } from "next/navigation"
import WithdrawModal from "@/components/modals/WithdrawModal"

export default function MypageSideMenu() {
    const [openWithdraw, setOpenWithdraw] = useState(false)
    const pathname = usePathname()
    console.log(pathname)

    return (
        <div className="w-[240px]">
            <ul className="mypage-side-menu flex flex-col gap-8">
                <li><h2><Link href="/mypage">마이페이지</Link></h2></li>
                <li>
                    <ul className="flex flex-col gap-2">
                        <h3>경매이력</h3>
                        <li><Link 
                            href="/mypage/buyAuction" 
                            className={`text-sm transition-all duration-300 ${
                                pathname === '/mypage/buyAuction' 
                                    ? 'text-[#F2B90C]' 
                                    : 'text-[#A5A5A5] hover:text-[#F2B90C]'
                            }`}
                        >입찰 경매 조회</Link></li>
                        <li><Link 
                            href="/mypage/sellAuction" 
                            className={`text-sm transition-all duration-300 ${
                                pathname === '/mypage/sellAuction' 
                                    ? 'text-[#F2B90C]' 
                                    : 'text-[#A5A5A5] hover:text-[#F2B90C]'
                            }`}
                        >판매 경매 조회</Link></li>
                        <li><Link 
                            href="/mypage/wishAuction" 
                            className={`text-sm transition-all duration-300 ${
                                pathname === '/mypage/wishAuction' 
                                    ? 'text-[#F2B90C]' 
                                    : 'text-[#A5A5A5] hover:text-[#F2B90C]'
                            }`}
                        >관심 경매</Link></li>
                    </ul>
                </li>
                <li>
                    <ul className="flex flex-col gap-2">
                        <h3>카드</h3>
                        <li><Link 
                            href="/mypage/wishItem" 
                            className={`text-sm transition-all duration-300 ${
                                pathname === '/mypage/wishItem' 
                                    ? 'text-[#F2B90C]' 
                                    : 'text-[#A5A5A5] hover:text-[#F2B90C]'
                            }`}
                        >관심 카드</Link></li>
                    </ul>
                </li>
                <li>
                    <ul className="flex flex-col gap-2">
                        <h3>내 정보</h3>
                        <li><Link 
                            href="/mypage/edit" 
                            className={`text-sm transition-all duration-300 ${
                                pathname === '/mypage/edit' 
                                    ? 'text-[#F2B90C]' 
                                    : 'text-[#A5A5A5] hover:text-[#F2B90C]'
                            }`}
                        >내 정보 수정</Link></li>
                        <li>
                            <button 
                                onClick={() => setOpenWithdraw(true)} 
                                className="text-[#A5A5A5] text-sm text-left w-full cursor-pointer hover:text-[#F2B90C] transition-all duration-300"
                            >회원탈퇴</button>
                        </li>
                    </ul>
                </li>
            </ul>

            <WithdrawModal
                isOpen={openWithdraw}
                onClose={() => setOpenWithdraw(false)}
                onConfirm={() => {
                    // TODO: 탈퇴 API 연동
                    setOpenWithdraw(false)
                }}
            />
        </div>
    )
}