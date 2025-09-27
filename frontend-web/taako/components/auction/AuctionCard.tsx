'use client'

// 경매 카드(경매 조회 시 사용)
import { GetAuction } from "@/types/auction"
import Image from "next/image"
import Link from "next/link";
import { useState, useEffect, useRef, useCallback, useMemo   } from "react";
import RankElement from "../atoms/RankElement";
import { Heart, CircleCheck } from 'lucide-react';
import { Badge } from "@/components/ui/badge"
import { addWishAuction, removeWishAuction } from '@/lib/wish'
import { normalizeAxiosError } from '@/lib/normalizeAxiosError'

type Props = {
  item: GetAuction
  /** 선택: 부모 리스트와 동기화가 필요하면 전달 */
  onWishChange?: (id: number, wished: boolean) => void
}

export default function AuctionCard({ item, onWishChange }: Props){
    const [remainingTime, setRemainingTime] = useState(item.remainingSeconds);

    const startAtMs = useMemo<number | undefined>(() => {
        const s =
        (item as any).startsAt ??
        (item as any).startAt ??
        (item as any).startTime ??
        (item as any).openAt;
        if (!s) return undefined;
        const t = new Date(s).getTime();
        return Number.isFinite(t) ? t : undefined;
    }, [item]);

    const [startLeft, setStartLeft] = useState<number | undefined>(() => {
        if (!startAtMs) return undefined;
        const delta = Math.max(0, Math.floor((startAtMs - Date.now()) / 1000));
        return delta;
    });

    const [wished, setWished] = useState<boolean>(item.wished)
    const [pending, setPending] = useState(false)
    const abortRef = useRef<AbortController | null>(null)

    useEffect(() => {
        if (remainingTime <= 0) return;

        const timer = setInterval(() => {
            setRemainingTime(prev => {
                if (prev <= 1) {
                    return 0;
                }
                return prev - 1;
            });

            if (startAtMs) {
                const left = Math.max(0, Math.floor((startAtMs - Date.now()) / 1000));
                setStartLeft(left);
            }
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

    const formatStartSoon = (seconds: number) => {
        if (seconds <= 0) return '시작';
        if (seconds < 3600) {
        // 1시간 미만이면 분 단위 올림
        const mins = Math.ceil(seconds / 60);
        return `시작 ${mins}분 전`;
        }
        // 1시간 이상이면 "시작 H시간 M분 전"
        const h = Math.floor(seconds / 3600);
        const m = Math.ceil((seconds % 3600) / 60);
        return `시작 ${h}시간 ${m}분 전`;
    };

    const phase: 'before' | 'running' | 'ended' = useMemo(() => {
        // 시작 시간이 주어졌고 아직 시작 전
        if (typeof startLeft === 'number' && startLeft > 0) return 'before';
        // 시작 이후 진행 중(남은 종료 시간이 있음)
        if (remainingTime > 0) return 'running';
        // 종료됨
        return 'ended';
    }, [startLeft, remainingTime]);

    const timeText = useMemo(() => {
        if (phase === 'before') return formatStartSoon(startLeft ?? 0);
        if (phase === 'running') return formatTime(remainingTime);
        return '마감';
    }, [phase, startLeft, remainingTime]);

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
                            width={200} 
                            height={300}
                            className="w-full h-full object-cover rounded"
                            unoptimized
                        />
                    </div>
                    {item.tokenId && (
                        <Badge
                            variant="secondary"
                            className="absolute top-2 left-1 pl-2 pr-1 bg-gradient-to-b from-green-400 to-green-900
                            rounded-full scale-80
                            flex items-center gap-1 font-weight leading-tight"
                            >
                            NFT
                            <CircleCheck  className="w-5 -translate-y-[0px]" />
                        </Badge>
                    )}
                    <div className="w-full p-4 relative flex flex-col gap-2 bg-white/50 backdrop-blur-lg text-black rounded-lg">
                        <h3 className="h-13">{item.title.length > 18 ? item.title.slice(0, 18) + "..." : item.title}</h3>
                        <div className="text-[20px] font-semibold">{item.currentPrice} TKC</div>
                        <div className="text-sm text-[#242424]">
                            <span>입찰 {item.bidCount}회 | </span>
                            <span className={phase === "ended" ? "text-red-500 font-semibold" : ""}>
                                {timeText}
                            </span>
                        </div>
                        <button
                            onClick={toggleWish}
                            onKeyDown={onHeartKeyDown}
                            aria-pressed={wished}
                            aria-label={wished ? '관심경매 해제' : '관심경매 추가'}
                            disabled={pending}
                            className={`absolute bottom-4 right-4 rounded-full cursor-pointer
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