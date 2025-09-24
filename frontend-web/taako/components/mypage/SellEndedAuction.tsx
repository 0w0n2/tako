'use client'

import Image from "next/image"
import { 
  LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid, 
  LabelList, ReferenceLine, ReferenceArea 
} from "recharts";
import { MySellAuctions, Bids } from "@/types/auth";
import { useMyInfo } from "@/hooks/useMyInfo";
import { useDelivery } from "@/hooks/useDelivery";
import AddTracking from "./delivery/AddTracking";
import SellDeliveryForm from "./delivery/SellDeliveryForm";

const dummy: MySellAuctions[] = [
  {
    auctionId: 1,
    code: "124124",
    title: "피카츄 사세요",
    startDatetime: "2025/02/03T10:00",
    endDatetime: "2025/09/24T08:00",
    isEnd: true,
    idDelivery: false,
    currentPrice: 124,
    imageUrl: "/no-image.jpg",
    bids: [
      { data:"2025/09/03T10:00", nickname:"asdf", price:40 },
      { data:"2025/09/05T10:00", nickname:"as12df", price:50 },
      { data:"2025/09/17T12:00", nickname:"hhhdf", price:90 },
      { data:"2025/09/24T10:00", nickname:"as12df", price:124 },
    ],
    delivery: {
      "status": "string",
      "existTrackingNumber": true,
      "existRecipientAddress": true,
      "existSenderAddress": true
    }
  }
]

// X축 포맷 함수
const formatDateForXAxis = (ts: number) => {
  const d = new Date(ts);
  return `${d.getMonth() + 1}/${d.getDate()}`;
};

// 커스텀 툴팁
const CustomTooltip = ({ active, payload }: any) => {
  if (active && payload && payload.length) {
    const bids: Bids[] = payload[0].payload.bids as Bids[];
    const price: number = payload[0].value as number;
    return (
      <div className="bg-[#191924] border border-[#353535] text-white p-3 rounded shadow-lg min-w-[120px]">
        {bids && bids.length > 0 ? (
          bids.map((bid, index) => (
            <div key={index} className="flex flex-col gap-1">
              <p>nickname : "{bid.nickname}"</p>
              <p className="text-green-500">price : {bid.price} TKC</p>
            </div>
          ))
        ) : (
          <p className="text-[#f2b90c] ml-auto">입찰 정보 없음</p>
        )}
      </div>
    );
  }
  return null;
};

// 차트 데이터 생성 (종료된 경매는 기존 입찰만 표시)
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
    .map(([dayKey, { bids, price }]) => ({ date: dayKey, bids, price, }));

  return chartData;
};

export default function SellOnGoingAuction() {
  const auction = dummy[0];
  const { endedSellAuctions } = useMyInfo();
  // console.log(endedSellAuctions)

  const { handlerGetAuctionDelivery, auctionDelivery } = useDelivery();

  const chartData = getChartData(auction);
  const today = new Date();
  const todayTs = new Date(today.getFullYear(), today.getMonth(), today.getDate()).getTime();
  const endDate = new Date(auction.endDatetime!.replace(/\//g, "-"));
  const endDateTs = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate()).getTime();

  return (
    <div>
      {endedSellAuctions.map((item, index) => (
        <div>
          here!
        </div>
      ))}
      {/* 배송지입력버튼 모달 */}
      <SellDeliveryForm auctionId={auction.auctionId} />
      {/* 송장번호입력버튼 모달 */}
      <AddTracking item={auctionDelivery} />

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
                  <p className="text-sm font-bold text-red-500">경매 종료</p>
                </div>
              </div>
              <div className="flex flex-col justify-center gap-2">
                <button className="px-8 py-3 text-sm rounded-md border-1 border-[#353535] bg-[#191924]">배송지등록</button>
                <button className="px-8 py-3 text-sm rounded-md border-1 border-[#353535] bg-[#191924]">송장번호입력</button>
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
            <Line type="monotone" dataKey="price" name="입찰가" stroke="#ffffff" strokeWidth={2} dot />
          </LineChart>
        </div>
    </div>
  );
}
