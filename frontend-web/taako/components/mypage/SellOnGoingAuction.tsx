'use client'

import Image from "next/image"
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion"

export default function AuctionCard() {
  const details = [
    { date: '25/03/22 08:23', bidder: 'pokemonhunter', price: '0.999 BTC' },
    { date: '25/03/22 05:10', bidder: 'cardlover', price: '0.801 BTC' },
  ]

  return (
    <div>
      <div className="h-3 bg-[#1F1F2D]"></div>
      <div className="flex justify-between border-b border-[#353535] px-6 py-4">
        <p className="text-sm">경매 번호 O-OR30296405</p>
        <p className="text-sm">남은 시간 4시간 32분 52초</p>
      </div>

      <div className="py-4 px-6 flex justify-between">
        <div className="flex items-center gap-5">
          <div className="rounded-lg overflow-hidden w-25 h-25">
            <Image className="w-full h-full object-cover" src="/no-image.jpg" alt="thumbnail" width={100} height={100} />
          </div>
          <div>
            <h3 className="bid">피카츄 사세요 백만볼트 짱짱맨...</h3>
            <p className="text-lg">입찰가 354.88 PKC</p>
          </div>
        </div>

        <div className="flex items-center gap-4">
          <div className="text-right">
            <p>
              현재 입찰가 <span className="text-green-500 ml-1">354.88 PKC</span>
            </p>
            <p>
              내 입찰가 <span className="text-red-500 ml-1">354.88 PKC</span>
            </p>
          </div>
        </div>
      </div>

      <Accordion type="single" collapsible className="bg-[#1F1F2D]">
        <AccordionItem value="history">
          <AccordionTrigger className="flex justify-center gap-2 py-3 [#eaeaea] hover:no-underline">
            입찰 기록 보기
          </AccordionTrigger>
          <AccordionContent className="px-6 pb-4">
            {details.map((d, idx) => (
              <div
                key={idx}
                className="flex justify-between py-2 border-b border-[#353535] last:border-0"
              >
                <span className="text-sm text-[#a5a5a5]">{d.date}</span>
                <span className="text-md">{d.bidder}</span>
                <span className="text-md">{d.price}</span>
              </div>
            ))}
          </AccordionContent>
        </AccordionItem>
      </Accordion>
    </div>
  )
}
