import Image from "next/image"

export default function Mypage(){
    return (
        <div className="flex flex-col gap-10">
            <div className="flex gap-4">
                {/* 기본정보, 지갑조회, 판매중경매, 입찰중경매, 리뷰조회 */}
                <div className="flex-1 flex flex-col gap-10">
                    <div className="flex gap-6 items-center">
                        <div className="w-30 h-30 rounded-full bg-gray-200 overflow-hidden"><img src="" alt="" /></div>
                        <div>
                            <p className="mb-1">nickname</p>
                            <div className="flex gap-1 items-center text-sm text-[#D2D2D2]">
                                <div><Image src="/icon/star.png" width={18} height={18} alt="" /></div>
                                <p>4.8</p>
                                <p>(12)</p>
                            </div>
                        </div>
                    </div>
                    <p className="text-sm text-[#D2D2D2]">TCG 10년차 전문가입니다. 많은 문의주세요</p>
                </div>

                {/* 지갑정보 */}
                <div className="flex-1 p-8 border-1 border-[#353535] bg-[#191924] rounded-xl flex justify-between">
                    <div className="flex flex-col justify-between">
                        <h3>내 지갑</h3>
                        <div className="flex flex-col gap-1">
                            <p className="text-[#D2D2D2] mb-1">보유자산</p>
                            <div className="flex justify-between gap-6"><p className="text-2xl text-[#A4B2FF] font-semibold">46,500,888</p><p className="text-[#D2D2D2]">KRW</p></div>
                            <div className="flex justify-between gap-6"><p className="text-2xl text-[#A4B2FF] font-semibold">3.0000</p><p className="text-[#D2D2D2]">TKC</p></div>
                        </div>
                    </div>
                    <ul className="flex flex-col gap-3">
                        <li className="py-3 px-12 border-1 border-[#353535]">코인교환</li>
                        <li className="py-3 px-12 border-1 border-[#353535]">충전하기</li>
                        <li className="py-3 px-12 border-1 border-[#353535]">송금하기</li>
                    </ul>
                </div>
            </div>

            {/* 판매목록 */}
            <div>
                <h2>판매 경매</h2>
            </div>

            {/* 리뷰 */}
            <div>
                <h2>리뷰</h2>
            </div>
        </div>
    )
}