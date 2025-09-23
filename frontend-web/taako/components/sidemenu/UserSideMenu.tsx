import Link from "next/link";
import { Button } from "@/components/ui/button"
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from "@/components/ui/sheet"
import { Menu, ChevronRight } from 'lucide-react';

import SideMenuBidHistory from "./SideMenuBidHistory";

export default function UserSideMenu(){
    return(
        <Sheet>
          <SheetTrigger asChild>
            <Menu className="cursor-pointer" />
          </SheetTrigger>
          <SheetContent>
            <SheetHeader>
              <SheetTitle>Nickname님 환영합니다</SheetTitle>
              <SheetDescription>
                최근 입찰내역을 확인하세요.
              </SheetDescription>
            </SheetHeader>
            {/* 내용 */}
            <div className="py-10">
              <div>
                <div className="flex items-end justify-between mb-6 border-b border-[#aaa] pb-2">
                  <h3>내 입찰 내역</h3>
                  <Link href="/mypage/buyAuction" className="flex items-center text-sm text-[#a5a5a5]">
                    자세히보기
                    <ChevronRight className="w-4" />
                  </Link>
                </div>
                <div className="flex flex-col gap-6">
                  {[1,2].map((item, index)=> (
                    <div key={`bid-history-${item}-${index}`}>
                      <div className="flex justify-between pb-2 border-b border-[#353535]">
                        <p className="text-sm">경매제목: 피카츄 팝니다 선제요</p>
                      </div>
                      <SideMenuBidHistory />
                    </div>
                  ))}
                </div>
              </div>

            </div>
            <SheetFooter className="flex !flex-col gap-2">
              <Link
                href="/auction/new"
                className="block px-8 py-3 bg-[#364153] text-center text-[#7DB7CD] border-1 border-[#7DB7CD] cursor-pointer rounded-lg
                hover:bg-[#3E4C63] transition-all duration-300">경매등록
              </Link>
              {/* <div>
                <Button type="submit">로그아웃</Button>
                <SheetClose asChild>
                  <Button variant="outline">Close</Button>
                </SheetClose>
              </div> */}
            </SheetFooter>
          </SheetContent>
        </Sheet>
    )
}