'use client'

import Image from "next/image";
import {Accordion, AccordionItem} from "@heroui/react";

export default function SellAuction(){
    const details = [
        { date: '25/03/22 08:23', bidder: 'pokemonhunter', price: '0.999 BTC' },
        { date: '25/03/22 05:10', bidder: 'cardlover', price: '0.801 BTC' },
    ]

    return(
        <div>
            {/* 헤더 */}
            <div className="flex items-center py-3 px-5">
                <div className="w-[150px] text-left font-light text-[#d5d5d5]">경매번호</div>
                <div className="w-[270px] text-left font-light text-[#d5d5d5]">상품명</div>
                <div className="w-[150px] text-left font-light text-[#d5d5d5]">날짜</div>
                <div className="w-[180px] text-left font-light text-[#d5d5d5]">입찰자</div>
                <div className="w-[90px] text-left font-light text-[#d5d5d5]">가격</div>
                <div className="w-[130px] font-light text-[#d5d5d5] text-center">상태</div>
                <div className="w-[150px]"></div>
            </div>

            <Accordion variant="splitted" className="p-0">
                <AccordionItem 
                    className="bg-[#191924] border border-[#353535] rounded-lg px-4"
                    key="1" 
                    aria-label="Accordion 1" 
                    indicator={({ isOpen }) => (
                        <Image className={`${isOpen ? "rotate-90" : ""} transition-transform duration-200`} src="/icon/arrow-down.svg" width={13} height={8} alt="detail" />
                    )}
                    title={
                        <div className="flex items-center w-full">
                            <div className="w-[150px] text-[#eaeaea] text-sm text-left">O-OR30296405</div>
                            <div className="w-[270px] text-[16px] text-left">피카츄 사세요 백만볼트 짱짱맨...</div>
                            <div className="w-[150px] text-sm text-[#a5a5a5] text-left">25/03/23 18:18</div>
                            <div className="w-[180px] text-[16px] text-left">Jake Lee</div>
                            <div className="w-[90px] text-left text-[16px]">1.053 BTC</div>
                            <div className="w-[130px] text-[16px] text-center">판매 중</div>
                        </div>
                    }
                >
                    {details.map((d, idx) => (
                        <div
                            key={idx}
                            className={`flex items-center [&>div]:py-3`}
                        >
                            <div className="w-[150px] text-[#eaeaea] text-sm"></div>
                            <div className="w-[270px] text-md"></div>
                            <div className="w-[150px] text-sm text-[#a5a5a5]">{d.date}</div>
                            <div className="w-[180px] text-md">{d.bidder}</div>
                            <div className="w-[90px] text-left text-md">{d.price}</div>
                            <div className="w-[130px] text-md text-center"></div>
                            <div className="w-[150px] py-4"></div>
                        </div>
                    ))}
                </AccordionItem>
                <AccordionItem 
                    className="bg-[#191924] border border-[#353535] rounded-lg px-4"
                    key="2" 
                    aria-label="Accordion 2" 
                    indicator={({ isOpen }) => (
                        <Image className={`${isOpen ? "rotate-90" : ""} transition-transform duration-200`} src="/icon/arrow-down.svg" width={13} height={8} alt="detail" />
                    )}
                    title={
                        <div className="flex items-center w-full">
                            <div className="w-[150px] text-[#eaeaea] text-sm text-left">O-OR30296405</div>
                            <div className="w-[270px] text-[16px] text-left">피카츄 사세요 백만볼트 짱짱맨...</div>
                            <div className="w-[150px] text-sm text-[#a5a5a5] text-left">25/03/23 18:18</div>
                            <div className="w-[180px] text-[16px] text-left">Jake Lee</div>
                            <div className="w-[90px] text-left text-[16px]">1.053 BTC</div>
                            <div className="w-[130px] text-[16px] text-center">판매 중</div>
                        </div>
                    }
                >
                    {details.map((d, idx) => (
                        <div
                            key={idx}
                            className={`flex items-center [&>div]:py-4`}
                        >
                            <div className="w-[150px] text-[#eaeaea] text-sm"></div>
                            <div className="w-[270px] text-md"></div>
                            <div className="w-[150px] text-sm text-[#a5a5a5]">{d.date}</div>
                            <div className="w-[180px] text-md">{d.bidder}</div>
                            <div className="w-[90px] text-left text-md">{d.price}</div>
                            <div className="w-[130px] text-md text-center"></div>
                            <div className="w-[150px] py-4"></div>
                        </div>
                    ))}
                </AccordionItem>
            </Accordion>
       </div>
    )
}