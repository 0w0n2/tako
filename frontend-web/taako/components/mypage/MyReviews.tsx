import Image from "next/image"
import { useMyInfo } from "@/hooks/useMyInfo";

const reviews = [
    {id:1,description:"카드 컨디션이 좋아요"},
    {id:2,description:"가격이 적당해요"},
    {id:3,description:"너무 소중해요"},
    {id:4,description:"감사합니다"},
]

export default function MyReviews({memberId}:{memberId:number}){
    // 종료 경매(리뷰) 조회
    const { endedAuctions } = useMyInfo();
    const onReviewAuctions = endedAuctions.filter((item) => item.delivery.status !== null)

    return(
        <div className="flex flex-col gap-6 border-b border-[#353535] px-5 py-6">
            <div className="flex justify-between items-center">
                <div className="flex gap-2">
                    {[1,2,3,4,5].map((index) => (
                        <Image className="w-4 object-fit cursor-pointer" src="/icon/star-on.png" alt={`star-on${index}`} width={100} height={100} />
                    ))}
                </div>
                <p className="text-sm">2025/09/17</p>
            </div>
            <div className="flex items-center gap-4">
                <div className="w-18 h-18 rounded-sm overflow-hidden"><Image className="w-full h-full object-fit" src="/no-image.jpg" alt="thumnail" width={100} height={100} /></div>
                <div>
                    <h3>포켓몬 팝니다 팝니다 팝니다!</h3>
                    <p className="">최종 입찰가 <span className="text-green-500 ml-1">482.34</span> PKC</p>
                </div>
            </div>
            <div className="flex gap-2">
                {reviews.map((item, index)=>(
                    <div className="text-sm px-1.5 p-1 rounded-sm bg-gray-300 text-black">
                        {item.description}
                    </div>
                ))}
            </div>
        </div>
    )
}