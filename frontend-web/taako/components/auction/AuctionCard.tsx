'use client'

// 경매 카드(경매 조회 시 사용)
import { GetAuction } from "@/types/auction"
import Image from "next/image"
import Link from "next/link";
import { useState, useEffect } from "react";
import RankElement from "../atoms/RankElement";
import { Heart } from 'lucide-react';

export default function AuctionCard({ item }: { item: GetAuction }){
    const [remainingTime, setRemainingTime] = useState(item.remainingSeconds);

    useEffect(() => {
        if (remainingTime <= 0) return;

        const timer = setInterval(() => {
            setRemainingTime(prev => {
                if (prev <= 1) {
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);

        return () => clearInterval(timer);
    }, []);

    // remainingTime 일, 시간, 분, 초 변환
    const formatTime = (seconds: number) => {
        if (seconds <= 0) return '마감';
        
        const days = Math.floor(seconds / (24 * 60 * 60));
        const hours = Math.floor((seconds % (24 * 60 * 60)) / (60 * 60));
        const minutes = Math.floor((seconds % (60 * 60)) / 60);
        const secs = seconds % 60;

        const parts = [];
        if (days > 0) parts.push(`${days}일`);
        if (hours > 0) parts.push(`${hours}시간`);
        if (minutes > 0) parts.push(`${minutes}분`);
        if (secs > 0) parts.push(`${secs}초`);

        // 0이 아닌 상위 2개만 선택
        return parts.slice(0, 2).join(' ');
    };

    return(
        <>
            <Link href={`/auction/${item.id}`}>
                <div className="relative border rounded-lg h-80 flex items-end overflow-hidden">
                    <div className="absolute top-2 right-2 z-1">
                        <RankElement rank={item.grade} />
                    </div>
                    <div className="absolute top-0 left-0 w-full h-full">
                        <Image 
                            src={item.primaryImageUrl || '/no-image.jpg'} 
                            alt={item.title}
                            width={300} 
                            height={300}
                            className="w-full h-full object-cover rounded"
                            unoptimized
                        />
                    </div>
                    <div className="w-full p-4 relative flex flex-col gap-2 bg-white/30 backdrop-blur-lg text-black rounded-lg">
                        <h3 className="h-13">{item.title.length > 18 ? item.title.slice(0, 18) + "..." : item.title}</h3>
                        <div className="text-[20px] font-semibold">{item.currentPrice} TKC</div>
                        <div className="text-sm text-[#242424]">
                            <span>입찰 {item.bidCount}회 | </span>
                            <span className={remainingTime <= 0 ? "text-red-500 font-semibold" : ""}>
                                {formatTime(remainingTime)}
                            </span>
                        </div>
                        <div className="absolute bottom-4 right-4">
                            {item.wished ? (
                                <Heart fill="red" stroke="red" className="w-5 h-5" />
                            ) : (
                                <Heart className="w-5 h-5" stroke="#242424" />
                            )}
                        </div>
                    </div>
                </div>
            </Link>
        </>
    )
}