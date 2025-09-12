import { AuctionDetailProps } from "@/types/auction"
import { useState, useEffect } from "react"

interface Time{
    props: AuctionDetailProps
}

export default function RemainingTime({ props }: Time){
    const [timeLeft, setTimeLeft] = useState({
        days: 0,
        hours: 0,
        minutes: 0,
        seconds: 0
    });

    useEffect(() => {
        const calculateTimeLeft = () => {
            const now = new Date().getTime();
            const endTime = new Date(props.endTime).getTime();
            const difference = endTime - now;

            if (difference > 0) {
                const days = Math.floor(difference / (1000 * 60 * 60 * 24));
                const hours = Math.floor((difference % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
                const minutes = Math.floor((difference % (1000 * 60 * 60)) / (1000 * 60));
                const seconds = Math.floor((difference % (1000 * 60)) / 1000);

                setTimeLeft({ days, hours, minutes, seconds });
            } else {
                setTimeLeft({ days: 0, hours: 0, minutes: 0, seconds: 0 });
            }
        };

        // 즉시 계산
        calculateTimeLeft();

        // 1초마다 업데이트
        const timer = setInterval(calculateTimeLeft, 1000);

        return () => clearInterval(timer);
    }, [props.endTime]);

    const formatTime = (value: number, unit: string) => {
        return `${value}${unit}`;
    };

    return(
        <div className="flex gap-2">
            {timeLeft.days > 0 && (
                <span className="text-[#7db7cd]">
                    {formatTime(timeLeft.days, '일')}
                </span>
            )}
            {timeLeft.hours > 0 && (
                <span className="text-[#7db7cd]">
                    {formatTime(timeLeft.hours, '시간')}
                </span>
            )}
            {timeLeft.minutes > 0 && (
                <span className="text-[#7db7cd]">
                    {formatTime(timeLeft.minutes, '분')}
                </span>
            )}
            <span className="text-[#7db7cd]">
                {formatTime(timeLeft.seconds, '초')}
            </span>
        </div>
    )
}