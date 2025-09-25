'use client'

import { CircleCheck, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { useAddress } from "@/hooks/useAddress"
import { useDelivery } from '@/hooks/useDelivery';
import { useEffect, useState } from 'react';

interface SellDeliveryFormProps {
  auctionId: number;
  onClose: () => void;
}

export default function SellDeliveryForm({ auctionId, onClose }: SellDeliveryFormProps) {
  const { address, defaultAddress } = useAddress();
  const { handlerSellerAddress } = useDelivery();

  const [addressId, setAddressId] = useState<number | null>(null);

  // defaultAddress가 로드되면 기본값으로 세팅
  useEffect(() => {
    if (defaultAddress && defaultAddress.length > 0) {
      setAddressId(defaultAddress[0].id);
    }
  }, [defaultAddress]);

  return (
    <div className="fixed inset-0 bg-black/50 flex justify-center items-center z-50">
      <div className="w-[450px] bg-gray-800 p-5 rounded-xl relative flex flex-col gap-6">
        <X className='absolute top-5 right-5 cursor-pointer' onClick={onClose} />

        <h3 className='text-center'>배송지 등록</h3>

        <div className="flex flex-col gap-3">
          {address.length > 0 ? (
            address.map((item) => {
              const isSelected = addressId === item.id;
              const isDefault = item.default;

              return (
                <div
                  key={item.id}
                  className={`p-4 border-1 border-[#666] rounded-lg flex justify-between items-center hover:bg-gray-700 cursor-pointer ${isSelected ? "bg-gray-700" : ""}`}
                  onClick={() => setAddressId(item.id)}
                >
                  <div>
                    <p>{item.placeName}</p>
                    <p className="text-sm text-[#a5a5a5]">{item.baseAddress}</p>
                  </div>
                  <div className="flex items-center gap-1 text-sm">
                    {isDefault && (
                      <span className={`${isSelected ? 'text-white' : 'text-[#a9a9a9]'}`}>
                        기본배송지
                      </span>
                    )}
                    <CircleCheck
                      className="w-5"
                      stroke={isSelected ? "#ffffff" : "#a9a9a9"}
                    />
                  </div>
                </div>
              )
            })
          ) : (
            <div className="p-4 text-center text-sm text-[#a5a5a5]">
              등록된 배송지가 없습니다. <br />
              <span className="text-white">마이페이지 &gt; 주소록</span> 에서 주소를 등록해주세요.
            </div>
          )}
        </div>

        <div className='grid grid-cols-2 gap-3'>
          <Button
            className='cursor-pointer h-12'
            variant="outline"
            onClick={() => {
              if (addressId !== null) {
                handlerSellerAddress(auctionId, addressId);
                onClose();
              } else {
                alert("배송지를 선택해주세요!");
              }
            }}
          >
            등록하기
          </Button>
          <Button
            className='cursor-pointer h-12'
            variant="outline"
            onClick={onClose}
          >
            취소
          </Button>
        </div>
      </div>
    </div>
  )
}
