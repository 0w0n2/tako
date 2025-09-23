'use client'

import { MyAuctionResponse } from "@/types/auction"
import Image from "next/image"
import Link from "next/link";
import { useState, useEffect } from "react";
import { Clock, Users, DollarSign } from 'lucide-react';

export default function MyAuctionCard({ item }: { item: MyAuctionResponse }){
    const [remainingTime, setRemainingTime] = useState(0);

    useEffect(() => {
        const endTime = new Date(item.endDatetime).getTime();
        const now = new Date().getTime();
        const timeLeft = Math.max(0, Math.floor((endTime - now) / 1000));
        setRemainingTime(timeLeft);

        if (timeLeft <= 0) return;

        const timer = setInterval(() => {
            setRemainingTime(prev => {
                if (prev <= 1) {
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);

        return () => clearInterval(timer);
    }, [item.endDatetime]);

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

        return parts.slice(0, 2).join(' ');
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    return(
        <Link href={`/auction/${item.auctionId}`}>
            <div className="relative border rounded-lg h-80 flex items-end overflow-hidden hover:shadow-lg transition-shadow">
                <div className="absolute top-0 left-0 w-full h-full">
                    <Image 
                        src={item.imageUrl || '/no-image.jpg'} 
                        alt={item.title}
                        width={300} 
                        height={300}
                        className="w-full h-full object-cover rounded"
                        unoptimized
                    />
                </div>
                
                {/* 상태 표시 */}
                <div className="absolute top-2 left-2 z-10">
                    {item.isEnd ? (
                        <span className="bg-red-500 text-white px-2 py-1 rounded text-xs font-semibold">
                            {item.closeReason || '마감'}
                        </span>
                    ) : (
                        <span className="bg-green-500 text-white px-2 py-1 rounded text-xs font-semibold">
                            진행중
                        </span>
                    )}
                </div>

                <div className="w-full p-4 relative flex flex-col gap-2 bg-white/30 backdrop-blur-lg text-black rounded-lg">
                    <h3 className="h-13 font-semibold">
                        {item.title.length > 18 ? item.title.slice(0, 18) + "..." : item.title}
                    </h3>
                    
                    <div className="flex items-center gap-1 text-[20px] font-semibold">
                        <DollarSign className="w-5 h-5" />
                        {item.currentPrice.toLocaleString()} TKC
                    </div>
                    
                    <div className="flex items-center gap-1 text-sm text-[#242424]">
                        <Users className="w-4 h-4" />
                        <span>입찰 {item.bids.length}회</span>
                    </div>
                    
                    <div className="flex items-center gap-1 text-sm">
                        <Clock className="w-4 h-4" />
                        <span className={remainingTime <= 0 ? "text-red-500 font-semibold" : ""}>
                            {formatTime(remainingTime)}
                        </span>
                    </div>
                    
                    <div className="text-xs text-gray-600">
                        시작: {formatDate(item.startDatetime)}
                    </div>
                    
                    {item.bids.length > 0 && (
                        <div className="text-xs text-gray-600">
                            최고입찰: {item.bids[0].nickname} ({item.bids[0].price.toLocaleString()} TKC)
                        </div>
                    )}
                </div>
            </div>
        </Link>
    )
}
