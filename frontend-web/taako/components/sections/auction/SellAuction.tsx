"use client"

import Image from "next/image"
import { useState } from "react"

export default function SellAuction(){
    const [isOpen, setIsOpen] = useState(false)
    const detailCount = 2
    const details = [
        { date: '25/03/22 08:23', bidder: 'pokemonhunter', price: '0.999 BTC' },
        { date: '25/03/22 05:10', bidder: 'cardlover', price: '0.801 BTC' },
    ]
    return(
        <div>
         <table className="w-full">
           {/* 헤더 */}
           <thead>
             <tr className="flex items-center py-3 px-5">
               <th className="w-[150px] text-left font-light text-[#d5d5d5]">경매번호</th>
               <th className="w-[270px] text-left font-light text-[#d5d5d5]">상품명</th>
               <th className="w-[150px] text-left font-light text-[#d5d5d5]">날짜</th>
               <th className="w-[180px] text-left font-light text-[#d5d5d5]">입찰자</th>
               <th className="w-[90px] text-left font-light text-[#d5d5d5]">가격</th>
               <th className="w-[130px] font-light text-[#d5d5d5]">상태</th>
               <th className="w-[150px]"></th>
             </tr>
           </thead>

           <tbody className="sell-auction-tbody bg-[#191924] border border-[#353535] rounded-[24px]">
            <tr className="flex items-center px-5">
                <td className="w-[150px] text-[#eaeaea] text-sm">O-OR30296405</td>
                <td className="w-[270px] text-md">피카츄 사세요 백만볼트 짱짱맨...</td>
                <td className="w-[150px] text-sm text-[#a5a5a5]">25/03/23 18:18</td>
                <td className="w-[180px] text-md">Jake Lee</td>
                <td className="w-[90px] text-left text-md">1.053 BTC</td>
                <td className="w-[130px] text-md text-center">판매 중</td>
                <td className="w-[150px] flex justify-end py-4">
                    <button onClick={() => setIsOpen(v => !v)} className="flex justify-end items-center gap-2 cursor-pointer select-none">
                        자세히보기
                        <Image className={`${isOpen ? "rotate-180" : "rotate-0"} transition-transform duration-200`} src="/icon/arrow-down.svg" width={13} height={8} alt="detail" />
                    </button>
                </td>
            </tr>
            {/* 입찰 내역 toggle (반복 렌더링: detailCount = 2) */}
            {details.map((d, idx) => (
                <tr
                    key={idx}
                    className={`flex items-center [&_td]:py-4 px-5 overflow-hidden transition-[max-height,opacity] duration-300 ease-in-out ${isOpen ? 'opacity-100' : 'opacity-0'}`}
                    style={{ maxHeight: isOpen ? '64px' : '0px' }}
                >
                    <td className="w-[150px] text-[#eaeaea] text-sm"></td>
                    <td className="w-[270px] text-md"></td>
                    <td className="w-[150px] text-sm text-[#a5a5a5]">{d.date}</td>
                    <td className="w-[180px] text-md">{d.bidder}</td>
                    <td className="w-[90px] text-left text-md">{d.price}</td>
                    <td className="w-[130px] text-md text-center"></td>
                    <td className="w-[150px] py-4"></td>
                </tr>
            ))}
           </tbody>
         </table>
       </div>
    )
}