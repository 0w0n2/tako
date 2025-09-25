'use client'

import { X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { useState } from 'react';
import { useDelivery } from '@/hooks/useSellDelivery';
import { GetAuctionDelivery } from "@/types/delivery";

interface AddTrackingProps {
  auctionId: number;
  item?: GetAuctionDelivery | null;
  onClose: () => void;
}

const statusMap: Record<string, string> = {
  WAITING: "배송준비중",
  IN_PROGRESS: "배송중",
  COMPLETED: "배송완료",
  CONFIRMED: "구매확정",
};

export default function AddTracking({ auctionId, item, onClose }: AddTrackingProps) {
  const { auctionDelivery, handlerTrackingNumber } = useDelivery();
  const [trackingNumber, setTrackingNumber] = useState<string>("");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!trackingNumber) {
      alert("송장번호를 입력해주세요!");
      return;
    }
    handlerTrackingNumber(auctionId, trackingNumber);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30" >
      <div className="w-[450px] bg-gray-800 p-5 rounded-xl relative flex flex-col gap-6 justify-center">
        {/* X 버튼 */}
        <X className='absolute top-5 right-5 cursor-pointer' onClick={onClose} />
        <h3 className='text-center'>송장번호 등록</h3>
        {auctionDelivery?.recipientAddress ? (
            <div className='flex flex-col gap-2'>
            <p>판매자 정보</p>
            <div className='bg-gray-700 rounded-lg p-4 flex flex-col gap-2'>
                <div className='flex items-center text-sm'><p className='w-20 text-[#ddd]'>이름</p><p>{auctionDelivery?.recipientAddress.name}</p></div>
                <div className='flex items-center text-sm'><p className='w-20 text-[#ddd]'>연락처</p><p>{auctionDelivery?.recipientAddress.phone}</p></div>
                <div className='flex items-center text-sm'><p className='w-20 text-[#ddd]'>우편번호</p><p>{auctionDelivery?.recipientAddress.zipcode}</p></div>
                <div className='flex items-center text-sm'><p className='w-20 text-[#ddd]'>기본주소</p><p>{auctionDelivery?.recipientAddress.baseAddress}</p></div>
                <div className='flex items-center text-sm'><p className='w-20 text-[#ddd]'>상세주소</p><p>{auctionDelivery?.recipientAddress.addressDetail}</p></div>
            </div>
            </div>
        ) : (
            <div className='bg-gray-700 rounded-lg text-center py-10 text-sm text-[#a5a5a5]'>판매자 정보가 없습니다.</div>
        )}

        {!auctionDelivery?.trackingNumber ? (
          <form className='flex flex-col gap-2' onSubmit={handleSubmit}>
            <p>송장번호 등록</p>
            <div className='flex gap-3'>
              <Input
                placeholder='송장번호를 입력해주세요'
                value={trackingNumber}
                onChange={(e) => setTrackingNumber(e.target.value)}
              />
              <Button type="submit" variant="outline" className='cursor-pointer w-25 h-12'>
                등록하기
              </Button>
            </div>
          </form>
        ) : (
          <div className='flex flex-col gap-2'>
            <p>배송상태</p>
            <div className='bg-gray-700 rounded-lg p-4 flex flex-col gap-2 items-center'>
              <p className='text-lg'>{statusMap[auctionDelivery?.status ?? ""] || "미배송"}</p>
              <p className='text-[#ddd]'>송장번호: {auctionDelivery.trackingNumber}</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
