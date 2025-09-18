import Image from "next/image";

import { Heart } from "lucide-react"
import { Button } from "@/components/ui/button";

export default function WishAuctionPage() {
  return (
    <div>
      <h2>관심 경매</h2>
      
      <div className="mt-6">
        <p className="text-sm mb-3">총 2건</p>
        <ul className="">
          <li className="relative border-b border-[#353535]">
            <div className="py-5 px-6 flex justify-between items-end">
              <div className="flex items-center gap-5">
                <div className="rounded-md overflow-hidden w-25 h-25"><Image className="w-full h-full object-cover" src="/no-image.jpg" alt="thumnail" width={100} height={100} /></div>
                <div className="">
                  <h3 className="bid">피카츄 사세요 백만볼트 짱짱맨...</h3>
                  <p>남은 시간:  3일 12시간 48분 42초</p>
                </div>
              </div>
              <p className="">현재 입찰가 <span className="text-green-500 ml-1">354.88 PKC</span></p>
              <div className="absolute top-5 right-5">
                <Button variant="secondary" size="icon" className="size-8">
                  <Heart />
                </Button>
              </div>
            </div>
          </li>
        </ul>
      </div>
    </div>
  );
}
