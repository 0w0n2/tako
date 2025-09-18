import Image from "next/image"
import { Button } from "@/components/ui/button"

export default function BuyOnGoingAuction(){
    return(
      <div>
        {[1,2,3].map((item, index)=>{
          return(
            <div key={index}>
              <div className="h-3 bg-[#1F1F2D]"></div>
              <div className="flex justify-between border-b border-[#353535] px-6 py-4">
                <p className="text-sm">경매 번호 O-OR30296405</p>
                <p className="text-sm">남은 시간 4시간 32분 52초</p>
              </div>
              <div className="py-4 px-6 flex justify-between">
                <div className="flex items-center gap-5">
                  <div className="rounded-lg overflow-hidden w-25 h-25"><Image className="w-full h-full object-cover" src="/no-image.jpg" alt="thumnail" width={100} height={100} /></div>
                  <div className="">
                    <h3 className="bid">피카츄 사세요 백만볼트 짱짱맨...</h3>
                    <p className="text-lg">입찰가 354.88 PKC</p>
                  </div>
                </div>
                <div className="flex items-center gap-4">
                  <div className="text-right">
                    <p className="">현재 입찰가 <span className="text-green-500 ml-1">354.88 PKC</span></p>
                    <p className="">내 입찰가 <span className="text-red-500 ml-1">354.88 PKC</span></p>
                  </div>
                  <div className="grid grid-cols-2 gap-3">
                    <Button className="bg-[#7DB7CD] h-12 w-[120px] text-md" variant="default">재입찰</Button>
                    <Button className="bg-[#7DB7CD] h-12 w-[120px] text-md" variant="default">즉시구매</Button>
                  </div>
                </div>
              </div>
            </div>
          )
        })}
      </div>
    )
}