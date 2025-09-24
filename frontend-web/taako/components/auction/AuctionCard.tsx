'use client'

// 경매 카드(경매 조회 시 사용)
import { GetAuction } from "@/types/auction"
import Image from "next/image"
import Link from "next/link";
import { useState, useEffect, useRef, useCallback } from "react";
import RankElement from "../atoms/RankElement";
import { Heart, ThumbsUp, ThumbsDown } from 'lucide-react';
import { addWishAuction, removeWishAuction } from '@/lib/wish'
import { normalizeAxiosError } from '@/lib/normalizeAxiosError'

type Props = {
  item: GetAuction
  /** 선택: 부모 리스트와 동기화가 필요하면 전달 */
  onWishChange?: (id: number, wished: boolean) => void
}

export default function AuctionCard({ item, onWishChange }: Props){
    const [remainingTime, setRemainingTime] = useState(item.remainingSeconds);
    const [wished, setWished] = useState<boolean>(item.wished)
    const [pending, setPending] = useState(false)
    const abortRef = useRef<AbortController | null>(null)
    const [likeCount, setLikeCount] = useState(0);
    const [dislikeCount, setDislikeCount] = useState(0);

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

    // 관심 경매 온오프
    const toggleWish = useCallback(async (e: React.MouseEvent | React.KeyboardEvent) => {
        // 카드로의 네비게이션 막기
        e.preventDefault()
        e.stopPropagation()
        if (pending) return

        const next = !wished
        setWished(next)                 // 낙관적 업데이트
        setPending(true)

        // 이전 요청 취소
        abortRef.current?.abort()
        const ctrl = new AbortController()
        abortRef.current = ctrl

        try {
        if (next) {
            await addWishAuction(item.id, ctrl.signal)
        } else {
            await removeWishAuction(item.id, ctrl.signal) // lib/wish.ts에서 delete에 data:{} 포함 권장
        }
        onWishChange?.(item.id, next)
        } catch (err) {
        const ne = normalizeAxiosError(err)
        if (!ne.canceled) {
            setWished(!next)            // 실패 시 롤백
            // 여기에 토스트/알림 붙이면 UX ↑
            console.error('WISH_TOGGLE_FAIL', ne.status, ne.code, ne.url, ne.data)
        }
        } finally {
        setPending(false)
        }
    }, [item.id, wished, pending, onWishChange])

    const onHeartKeyDown = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter' || e.key === ' ') {
        toggleWish(e)
        }
    }

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
                        <button
                            onClick={toggleWish}
                            onKeyDown={onHeartKeyDown}
                            aria-pressed={wished}
                            aria-label={wished ? '관심경매 해제' : '관심경매 추가'}
                            disabled={pending}
                            className={`absolute bottom-4 right-4 p-2 rounded-full bg-white/70 hover:bg-white transition
                                        ${pending ? 'opacity-60 cursor-wait' : ''}`}
                        >
                            <Heart
                            className="w-5 h-5"
                            stroke={wished ? '#ff5a5a' : '#242424'}
                            fill={wished ? '#ff5a5a' : 'none'}
                            />
                        </button>
                    </div>
                </div>
            </Link>
        </>
    )
}