'use client'

import Image from "next/image";
import { 
  LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid, 
  LabelList, ReferenceLine, ReferenceArea 
} from "recharts";
import { MySellAuctions, Bids } from "@/types/auth";
import { useState, useEffect, useMemo } from "react";

const dummy: MySellAuctions[] = [
  {
    auctionId: 1,
    code: "124124",
    title: "피카츄 사세요",
    startDatetime: "2025/02/03T10:00",
    endDatetime: "2025/09/30T18:00",
    isEnd: false,
    idDelivery: false,
    currentPrice: 124,
    imageUrl: "/no-image.jpg",
    bids: [
      { data:"2025/09/03T10:00", nickname:"asdf", price:40 },
      { data:"2025/09/05T10:00", nickname:"as12df", price:50 },
      { data:"2025/09/17T12:00", nickname:"hhhdf", price:90 },
      { data:"2025/09/24T10:00", nickname:"as12df", price:124 },
    ]
  }
];

// 남은 시간 계산 (초 계산 제거)
function getRemainingTime(endDatetime: string) {
  const end = new Date(endDatetime.replace(/\//g, "-")).getTime();
  const now = new Date().getTime();
  let diff = end - now;
  if (diff <= 0) return { days: 0, hours: 0, minutes: 0 };

  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  diff -= days * (1000 * 60 * 60 * 24);
  const hours = Math.floor(diff / (1000 * 60 * 60));
  diff -= hours * (1000 * 60 * 60);
  const minutes = Math.floor(diff / (1000 * 60));

  return { days, hours, minutes };
}

// 예상 낙찰가 계산
function predictNextBid(auction: MySellAuctions): number {
  const bids = [...auction.bids]
    .filter(bid => bid.data && bid.price !== null)
    .sort(
      (a, b) =>
        new Date(a.data!.replace(/\//g, "-")).getTime() - 
        new Date(b.data!.replace(/\//g, "-")).getTime()
    );

  if (bids.length < 2) return auction.currentPrice ?? 0;

  let totalDiff = 0;
  for (let i = 1; i < bids.length; i++) {
    totalDiff += (bids[i].price! - bids[i - 1].price!);
  }
  const avgPriceIncrease = totalDiff / (bids.length - 1);

  const endTime = new Date(auction.endDatetime!.replace(/\//g, "-"));
  const lastBidTime = new Date(bids[bids.length - 1].data!.replace(/\//g, "-"));
  const remainingDays = (endTime.getTime() - lastBidTime.getTime()) / (1000 * 60 * 60 * 24);

  let avgDays = 0;
  for (let i = 1; i < bids.length; i++) {
    avgDays +=
      (new Date(bids[i].data!.replace(/\//g, "-")).getTime() - 
        new Date(bids[i - 1].data!.replace(/\//g, "-")).getTime()) /
      (1000 * 60 * 60 * 24);
  }
  avgDays = avgDays / (bids.length - 1);

  const remainingBids = Math.ceil(remainingDays / avgDays);

  return Math.round(bids[bids.length - 1].price! + avgPriceIncrease * remainingBids);
}

// 커스텀 툴팁
const CustomTooltip = ({ active, payload }: any) => {
  if (active && payload && payload.length) {
    const bids: Bids[] = payload[0].payload.bids as Bids[];
    const price: number = payload[0].value as number;
    return (
      <div className="bg-[#191924] border border-[#353535] text-white p-3 rounded shadow-lg min-w-[120px]">
        {bids && bids.length > 0 ? (
          bids.map((bid, index) => (
            <div
              key={`${bid.data ?? ''}-${bid.nickname ?? ''}-${index}`}
              className="flex flex-col gap-1"
            >
              <p>nickname : "{bid.nickname ?? 'unknown'}"</p>
              <p className="text-green-500">price : {bid.price ?? 0} TKC</p>
            </div>
          ))
        ) : (
          <p className="text-[#f2b90c] ml-auto">예상 낙찰가: {price} TKC</p>
        )}
      </div>
    );
  }
  return null;
};

// X축 포맷
const formatDateForXAxis = (ts: number) => {
  const d = new Date(ts);
  return `${d.getMonth() + 1}/${d.getDate()}`;
};

// 차트 데이터
const getChartData = (auction: MySellAuctions) => {
  if (!auction.bids || auction.bids.length === 0) return [];

  const parseDate = (str: string) => new Date(str.replace(/\//g, "-"));
  const dailyMaxMap = new Map<number, { bids: Bids[]; price: number }>();

  auction.bids.forEach((bid) => {
    if (!bid.data || bid.price === null) return;
    const ts = parseDate(bid.data).getTime();
    const dayKey = new Date(ts).setHours(0, 0, 0, 0);

    if (!dailyMaxMap.has(dayKey)) {
      dailyMaxMap.set(dayKey, { bids: [bid], price: bid.price });
    } else {
      const current = dailyMaxMap.get(dayKey)!;
      if (bid.price > current.price) {
        dailyMaxMap.set(dayKey, { bids: [bid], price: bid.price });
      } else {
        current.bids.push(bid);
      }
    }
  });

  const chartData = Array.from(dailyMaxMap.entries())
    .sort((a, b) => a[0] - b[0])
    .map(([dayKey, { bids, price }]) => ({
      date: dayKey,
      bids,
      price,
    }));

  const predictedPrice = predictNextBid(auction);
  const endDate = parseDate(auction.endDatetime!);
  chartData.push({
    date: new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate()).getTime(),
    bids: [],
    price: predictedPrice,
  });

  return chartData;
};

// 커스텀 라벨
const CustomLabel = (props: any) => {
  const { x, y, value, index, data } = props;
  if (index === data.length - 1) {
    return (
      <text
        x={x}
        y={y - 12}
        fill="#f2b90c"
        fontSize={14}
        textAnchor="middle"
      >
        예상: {value}TKC
      </text>
    );
  }
  return null;
};

export default function SellOnGoingAuction() {
  const auction = dummy[0];
  const chartData = useMemo(() => getChartData(auction), [auction.bids, auction.endDatetime]);

  const today = new Date();
  const todayTs = new Date(today.getFullYear(), today.getMonth(), today.getDate()).getTime();
  const endDate = new Date(auction.endDatetime!.replace(/\//g, "-"));
  const endDateTs = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate()).getTime();

  const [remaining, setRemaining] = useState(getRemainingTime(auction.endDatetime!));
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    const interval = setInterval(() => {
      setRemaining(getRemainingTime(auction.endDatetime!));
    }, 60000); // 1분마다 갱신
    return () => clearInterval(interval);
  }, [auction.endDatetime]);

  return (
    <div className="grid grid-cols-2 gap-3 py-5 pt-8 border-b border-[#353535]">
      <div className="flex flex-col justify-between">
        <div className="flex flex-col gap-1 px-4">
          <p className="text-sm text-[#a5a5a5]">경매 번호: {auction.code}</p>
          <h3 className="bid">{auction.title}</h3>
        </div>
        <div className="py-4 px-4 flex justify-between">
          <div className="flex items-center gap-5">
            <div className="rounded-lg overflow-hidden w-25 h-25">
              <Image
                className="w-full h-full object-cover"
                src={auction.imageUrl || "/no-image.jpg"}
                alt="thumbnail"
                width={100}
                height={100}
              />
            </div>
            <div>
              <p className="text-xl mb-1">입찰가: {auction.currentPrice} TKC</p>
              {mounted && (
                <p className="text-sm">
                  남은 시간: {remaining.days}일 {remaining.hours}시간 {remaining.minutes}분
                </p>
              )}
            </div>
          </div>
        </div>
      </div>

      <LineChart width={540} height={164} data={chartData} margin={{ top: 20, right:40 }}>
        <CartesianGrid stroke="#222" strokeDasharray="3 3" />
        <XAxis
          dataKey="date"
          type="number"
          domain={['dataMin', 'dataMax']}
          tickFormatter={formatDateForXAxis}
          tick={{ fill: "#a5a5a5", fontSize: 14, dy: 8 }}
          axisLine={false}
        />
        <YAxis tick={{ fill: "#aaa", fontSize: 12, dx: -4}} axisLine={false} />
        <Tooltip content={<CustomTooltip />} />
        <ReferenceArea x1={todayTs} x2={endDateTs} fill="#353535" fillOpacity={0.3} />
        <ReferenceLine x={todayTs} stroke="#00ff00" strokeWidth={2} label={{ position: "top", value: "오늘", fill: "#00ff00", dy:-2, fontSize: 12 }} />
        <Line type="monotone" dataKey="price" name="입찰가" stroke="#ffffff" strokeWidth={2} dot>
          <LabelList content={(props) => <CustomLabel {...props} data={chartData} />} />
        </Line>
      </LineChart>
    </div>
  );
}
