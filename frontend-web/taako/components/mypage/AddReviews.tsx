import Image from "next/image"

export default function AddReviews(){
    return(
        <div>
            <div className="bg-[#1F1F2D] p-3 text-sm text-[#aaa]">리뷰 작성하고 최대 <span>100P</span> 적립 받으세요.</div>
            <div className="flex flex-col gap-6 border-b border-[#353535]  px-5 py-6">
                <div className="flex items-center gap-4">
                    <div className="w-18 h-18 rounded-sm overflow-hidden"><Image className="w-full h-full object-fit" src="/no-image.jpg" alt="thumnail" width={100} height={100} /></div>
                    <div>
                        <h3>포켓몬 팝니다 팝니다 팝니다!</h3>
                        <p className="">최종 입찰가 <span className="text-green-500 ml-1">482.34</span> PKC</p>
                    </div>
                </div>
                <div className="flex gap-3 pl-2">
                    {[1,2,3,4,5].map((item, index) => (
                        <Image className="w-10 object-fit cursor-pointer" src="/icon/star-off.png" alt="star-off" width={100} height={100} />
                    ))}
                </div>
                <p className="text-[#a5a5a5] text-sm">별점을 선택하세요.</p>
            </div>
        </div>
    )
}