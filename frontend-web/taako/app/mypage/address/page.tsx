import { Button } from "@/components/ui/button"

export default function MyAddressPage(){
    return(
        <div>
            <div className="flex justify-between items-end">
                <div>
                    <h2>주소록</h2>
                </div>
                <Button
                    variant="outline"
                >+ 새 주소 추가하기</Button>
            </div>
            <div className="py-40 text-sm text-[#a5a5a5] text-center">
            배송지 정보가 없습니다.<br/>
            새 주소를 등록해주세요
            </div>
        </div>
    )
}