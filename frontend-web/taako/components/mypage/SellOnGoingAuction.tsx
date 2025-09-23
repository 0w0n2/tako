'use client'

import Image from "next/image"
import { LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid, LabelList } from "recharts";
import { MySellAuctions } from "@/types/auth";

const dummy: MySellAuctions[] = [
  {
    auctionId: 1,
    code: "124124",
    title: "피카츄 사세요",
    startDatetime: "2025/02/03T10:00",
    endDatetime: "2025/09/30T18:00",
    isEnd: false,
    currentPrice: 124,
    imageUrl: "/no-image.jpg",
    bids: [
      { data:"2025/02/03T10:00", nickname:"asdf", price:40 },
      { data:"2025/02/05T10:00", nickname:"as12df", price:50 },
      { data:"2025/02/05T12:00", nickname:"hhhdf", price:90 },
      { data:"2025/09/24T10:00", nickname:"as12df", price:124 },
    ]
  }
]

// 예상낙찰가 로직
function predictNextBid(auction: MySellAuctions) {
  const bids = [...auction.bids].sort(
    (a, b) =>
      new Date(a.data!.replace(/\//g, "-")).getTime() -
      new Date(b.data!.replace(/\//g, "-")).getTime()
  );

  if (bids.length < 2) return auction.currentPrice;

  let totalDiff = 0;
  for (let i = 1; i < bids.length; i++) {
    totalDiff += bids[i].price! - bids[i - 1].price!;
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
    const bids = payload[0].payload.bids;
    const price = payload[0].value;
    return (
      <div className="bg-gray-900 text-white p-3 rounded shadow-lg min-w-[120px]">
        {bids && bids.length > 0 ? (
          bids.map((bid: any, index: number) => (
            <p key={index} className="text-white text-sm mb-1">
              nickname: {bid.nickname} <br />
              price: {bid.price}
            </p>
          ))
        ) : (
          <p className="text-gray-400 text-sm">예상 낙찰가: {price}</p>
        )}
      </div>
    );
  }
  return null;
};

// X축 포맷 함수
const formatDateForXAxis = (dateStr: string) => {
  const cleanStr = dateStr.replace(" 예상", "");
  const d = new Date(cleanStr.replace(/\//g, "-"));
  return `${d.getMonth() + 1}/${d.getDate()}`; // MM/DD
};

// 차트 데이터 생성
const getChartData = (auction: MySellAuctions) => {
  if (!auction.bids || auction.bids.length === 0) return [];

  const parseDate = (str: string) => new Date(str.replace(/\//g, "-"));

  const sortedBids = [...auction.bids].sort(
    (a, b) => parseDate(a.data!).getTime() - parseDate(b.data!).getTime()
  );

  const chartData = sortedBids.map((bid) => ({
    date: bid.data,
    bids: [{ nickname: bid.nickname, price: bid.price }],
    price: bid.price,
  }));

  // endDatetime을 마지막 점으로 (예상 낙찰가)
  const predictedPrice = predictNextBid(auction);
  const endDate = parseDate(auction.endDatetime!);

  chartData.push({
    date: endDate.toISOString().split("T")[0] + " 예상",
    bids: [],
    price: predictedPrice,
  });

  return chartData;
};

// 오늘 날짜 dot 색상 표시
const getDotColor = (data: any) => {
  const today = new Date();
  const dotDateStr = data.date.replace(" 예상", "").replace(/\//g, "-");
  const dotDate = new Date(dotDateStr);

  if (
    today.getFullYear() === dotDate.getFullYear() &&
    today.getMonth() === dotDate.getMonth() &&
    today.getDate() === dotDate.getDate()
  ) {
    return "#00ff00"; // 초록색
  }
  return "#ffffff"; // 기본 흰색
};

export default function SellOnGoingAuction() {
  const auction = dummy[0];
  const chartData = getChartData(auction);

  return (
    <div className="grid grid-cols-2 gap-3">
      <div>
        <div className="flex justify-between border-b border-[#353535] px-6 py-4">
          <p className="text-sm">경매 번호 {auction.code}</p>
          <p className="text-sm">
            남은 시간{" "}
            {Math.floor(
              (new Date(auction.endDatetime!.replace(/\//g, "-")).getTime() -
                new Date().getTime()) /
                (1000 * 60 * 60)
            )}
            시간
          </p>
        </div>

        <div className="py-4 px-6 flex justify-between">
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
              <h3 className="bid">{auction.title}</h3>
              <p className="text-lg">입찰가 {auction.currentPrice} PKC</p>
            </div>
          </div>
        </div>
      </div>

      <LineChart width={540} height={200} data={chartData}>
        <CartesianGrid stroke="#222" strokeDasharray="3 3" />
        <XAxis
          dataKey="date"
          tick={{ fill: "#aaa" }}
          axisLine={false}
          type="category"
          tickFormatter={formatDateForXAxis}
        />
        <YAxis tick={{ fill: "#aaa" }} axisLine={false} />
        <Tooltip content={<CustomTooltip />} />
        <Line
          type="monotone"
          dataKey="price"
          name="입찰가"
          stroke="#ffffff"
          strokeWidth={2}
          dot={(props) => (
            <circle
              {...props}
              r={4}
              fill={getDotColor(props.payload)}
            />
          )}
        >
          <LabelList dataKey="price" position="top" fill="#f2b90c" fontSize={12} />
        </Line>
      </LineChart>
    </div>
  );
}
