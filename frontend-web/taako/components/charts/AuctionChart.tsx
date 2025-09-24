"use client";

import { AuctionDetailProps } from "@/types/auction"
import { LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid } from "recharts";

interface History {
    props: AuctionDetailProps
}
export default function AuctionChart({ props }: History) {
    const weekDays = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
    
    // 데이터 검증 및 변환
    const chartData = props.weeklyAuctions
        ?.filter(d => d && d.date)
        ?.map(d => {
            const date = new Date(d.date);
            // 유효한 날짜인지 확인
            if (isNaN(date.getTime())) {
                console.warn('Invalid date:', d.date);
                return null;
            }
            return {
                ...d,
                weekDay: weekDays[date.getDay()],
            };
        })
        ?.filter(Boolean) || [];

    const CustomTooltip = ({ active, payload, label }: any) => {
        if (active && payload && payload.length) {
            return (
                <div className="bg-gray-900 text-white p-3 rounded shadow-lg min-w-[120px]">
                    <p className="font-semibold mb-1">{label}</p>
                    {payload.map((entry: any) => (
                        <p key={entry.dataKey} className={`text-md mb-1 ${entry.dataKey === "avgPrice" ? "text-gray-400" : "text-white"}`}>
                            {entry.name}: {entry.value}
                        </p>
                    ))}
                </div>
            );
        }
        return null;
    };

    // 날짜 포매팅 함수
    const formatHistoryItem = (item: any) => {
        const date = new Date(item.createdAt);
        const formattedDate = date.toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            weekday: 'short'
        }).replace(/\./g, '').replace(/\s/g, '. ');
        
        const formattedTime = date.toLocaleTimeString('ko-KR', {
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false
        });
        
        return { formattedDate, formattedTime };
    };

    return (
        <div>
            {chartData.length > 0 ? (
                <LineChart width={540} height={200} data={chartData}>
                    <CartesianGrid stroke="#222" strokeDasharray="3 3" />
                    <XAxis dataKey="weekDay" tick={{ fill: "#aaa" }} axisLine={false} />
                    <YAxis tick={{ fill: "#aaa" }} axisLine={false} />
                    <Tooltip content={<CustomTooltip />} />
                    <Line type="monotone" dataKey="maxPrice" name="최대경매가" stroke="#ffffff" strokeWidth={2} dot={false} />
                    <Line type="monotone" dataKey="avgPrice" name="평균경매가" stroke="#888888" strokeWidth={2} dot={false} />
                    <Line type="monotone" dataKey="minPrice" name="최소경매가" stroke="#ffffff" strokeWidth={2} dot={false} />
                </LineChart>
            ) : (
                <div className="w-[540px] h-[200px] flex items-center justify-center bg-gray-800 rounded border border-gray-700">
                    <p className="text-gray-400 text-sm">지난 7일간의 거래 데이터가 없습니다.</p>
                </div>
            )}

            {/* 히스토리 테이블 */}
            <div className="">
                <div className="flex py-2 px-2 mt-3 border-b border-[#353535] text-[#a5a5a5] text-md">
                    <div className="flex-2 text-left">날짜</div>
                    <div className="flex-1 text-right">입찰액</div>
                    <div className="flex-1 text-right">입찰자</div>
                </div>
                
                <div className="flex flex-col gap-3 py-3 border-b border-[#353535] px-2">
                    {props.history.map((item, index) => {
                        const { formattedDate, formattedTime } = formatHistoryItem(item);
                        
                        return (
                            <div key={index} className="flex text-sm text-[#a5a5a5]">
                                <div className="flex-2 text-left">
                                    {formattedDate} {formattedTime}
                                </div>
                                <div className="flex-1 text-right">
                                    <div className="text-[#a5a5a5] font-medium">{item.amount} TKC</div>
                                </div>
                                <div className="flex-1 text-right">
                                    <div className="text-[#a5a5a5]">{item.bidderNickname}</div>
                                </div>
                            </div>
                        );
                    })}
                </div>
                
                <div className="mt-4 text-center">
                    <button className="w-full py-3 border border-[#353535] rounded text-[#a5a5a5] hover:bg-[#191924] cursor-pointer transition-colors">
                        입찰 내역 더보기
                    </button>
                </div>
            </div>
        </div>
    );
}