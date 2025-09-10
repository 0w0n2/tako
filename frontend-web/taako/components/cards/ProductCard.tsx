import Image from "next/image";
import Link from "next/link";
import WishButton from "../atoms/Button/WishButton"
import RankElement from "../atoms/RankElement"

// 경매 등록시 카드
export default function ProductCard() {
    return (
        <div>
            <Link href={`/auction/1`}>
                <div className="relative rounded-xl overflow-hidden h-[340px]">
                    {/* 좋아요 버튼 */}
                    <div className="absolute top-4 left-4"><WishButton /></div>
                    {/* 품질(랭크) */}
                    <div className="absolute top-4 right-4"><RankElement rank="S+" /></div>
                    
                    <div className="absolute bottom-0 left-0 w-full p-4 rounded-md bg-white/50 text-black">
                        <h3>내 친구 피카츄 절친볼트 맞을사람?</h3>
                        <p className="text-[22px] font-bold my-1">1.62 BTC</p>
                        <div className="flex gap-1 items-center text-sm">
                            <p>입찰 12회 | </p>
                            <div className="flex items-center gap-1">
                                <Image src="/icon/time.svg" alt="time-icon" width={11} height={11} />
                                <p>3시간 23분</p>
                            </div>
                        </div>
                    </div>

                    <Image src="/auction/351675401_1_1755660178_w1200.webp" alt="auction-item" width={100} height={100}
                        className="w-full h-full object-fit"/>
                </div>
            </Link>
        </div>
    );
}