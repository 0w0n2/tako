import Image from "next/image"
import { Button } from "@/components/ui/button"

export default function BuyOnGoingAuction(){
    return(
      <div className="pt-3 bg-[#070712]">
        <div className="flex justify-between border-b border-[#353535] bg-[#191924] px-6 py-4">
          <p className="text-sm">경매 번호 O-OR30296405</p>
          <p className="text-sm">남은 시간 4시간 32분 52초</p>
        </div>
        <div className="py-4 px-6 flex justify-between bg-[#191924]">
          <div className="flex items-center gap-3">
            <div><Image src="/no-image.jpg" alt="thumnail" width={100} height={100} /></div>
            <div className="">
              <h3>피카츄 사세요 백만볼트 짱짱맨...</h3>
              <p>입찰가 354.88 PKC</p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <div>
              <p>현재 입찰가 354.88 PKC</p>
              <p>내 입찰가 354.88 PKC</p>
            </div>
            <div className="grid grid-cols-2 gap-2">
              <Button variant="default">재입찰</Button>
              <Button variant="default" disabled>즉시구매</Button>
            </div>
          </div>
        </div>
      </div>
    )
}