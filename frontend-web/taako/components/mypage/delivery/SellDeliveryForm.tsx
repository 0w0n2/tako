import { CircleCheck } from 'lucide-react';
import { Button } from '@/components/ui/button';

// 1. 내 주소 조회 (response: addressId)
import { useAddress } from "@/hooks/useAddress"
// 2. 내 주소 선택(addressId)

// 3. 판매자: 보내는 주소 설정 (request: auctionId, addressId)
import { useDelivery } from '@/hooks/useDelivery';

export default function SellDeliveryForm({auctionId} : {auctionId: number}){
    const { address, defaultAddress } = useAddress();
    const { handlerSellerAddress } = useDelivery();
    // console.log(address)
    // console.log(defaultAddress[0])

    return(
        <div className="w-[450px]">
            <div className="flex flex-col gap-3">
                {address.map((item, index) => (
                    <div key={index} className="p-4 bg-gray-800 rounded-lg flex justify-between items-center">
                        <div>
                            <p className="">{item.placeName}</p>
                            <p className="text-sm text-[#a5a5a5]">{item.baseAddress}</p>
                        </div>
                        {item.default ? (
                            <div className="flex items-center gap-1 text-sm">
                                기본배송지
                                <CircleCheck className="w-5" />
                            </div>
                        ) : (
                            <CircleCheck className="w-5" stroke="#a9a9a9" />
                        )}
                    </div>
                ))}
            </div>
            <div className='grid grid-cols-2 gap-2'>
                <Button variant="outline" onClick={() => {handlerSellerAddress(auctionId, defaultAddress[0].id)}}>등록하기</Button>
                <Button variant="outline">취소</Button>
            </div>
        </div>
    )
}