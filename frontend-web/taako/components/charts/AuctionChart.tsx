"use client";

import { AuctionDetailProps } from "@/types/auction";
import { formatKSTCompact } from "@/lib/formatKST";
import { LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid } from "recharts";

interface CustomTooltipProps {
	active?: boolean;
	payload?: any[];
	label?: string;
}

function CustomTooltip({ active, payload, label }: Readonly<CustomTooltipProps>) {
	if (!active || !payload?.length) return null;
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

interface HistoryProps {
	readonly props: AuctionDetailProps;
	readonly realtimeBids?: ReadonlyArray<{ time: string; nickname: string; amount: number; type: "bid" | "buy_now" }>;
	readonly maxRows?: number; // 표시 최대 행 수 (기본 15)
}

export default function AuctionChart({ props, realtimeBids = [], maxRows = 15 }: Readonly<HistoryProps>) {
	const weekDays = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

	// 데이터 검증 및 변환
	const chartData =
		props.weeklyAuctions
			?.filter((d) => d?.date)
			?.map((d) => {
				if (!d) return null;
				const date = new Date(d.date);
				if (isNaN(date.getTime())) {
					console.warn("Invalid date:", d.date);
					return null;
				}
				return { ...d, weekDay: weekDays[date.getDay()] };
			})
			?.filter(Boolean) ?? [];

	// (내부 tooltip / format 함수 외부화 및 제거 - 린트)

	// 실시간 + 기존 history 병합 (실시간이 위, 시간 내림차순)
	const parsedRealtime = realtimeBids
		.map((r) => ({
			time: r.time,
			nickname: r.nickname,
			amount: r.amount,
			type: r.type,
			isRealtime: true as const,
		}))
		.sort((a, b) => new Date(b.time).getTime() - new Date(a.time).getTime());

	const legacy = (props.history || []).map((h) => ({
		time: h.createdAt,
		nickname: h.bidderNickname,
		amount: h.amount,
		type: "bid" as const,
		isRealtime: false as const,
	}));

	const merged = [...parsedRealtime, ...legacy].sort((a, b) => new Date(b.time).getTime() - new Date(a.time).getTime()).slice(0, maxRows);

	return (
		<div>
			{/* 히스토리 테이블 */}
			<div className="mt-6">
				<div className="flex py-2 px-2 border-b border-[#353535] text-[#a5a5a5] text-md">
					<div className="flex-[1.6] text-left">시간</div>
					<div className="flex-1 text-right">유형</div>
					<div className="flex-1 text-right">입찰액(ETH)</div>
					<div className="flex-1 text-right">입찰자</div>
				</div>
				<div className="flex flex-col gap-2 py-3 border-b border-[#353535] px-2 text-sm">
					{merged.length === 0 && <div className="text-[#666]">표시할 입찰 내역이 없습니다.</div>}
					{merged.map((row, idx) => {
						const label = formatKSTCompact(row.time);
						return (
							<div key={`${row.time}_${row.nickname}_${idx}`} className={`flex items-center ${row.isRealtime ? "text-[#e2f6ff]" : "text-[#a5a5a5]"}`}>
								<div className="flex-[1.6] text-left tabular-nums">{label}</div>
								<div className="flex-1 text-right">
									<span className={row.type === "buy_now" ? "text-[#ffb347] font-medium" : "text-[#7DB7CD]"}>{row.type === "buy_now" ? "즉시구매" : "입찰"}</span>
									{row.isRealtime && <span className="ml-1 text-[10px] bg-[#1e3a46] text-[#7DB7CD] px-1 py-[1px] rounded">LIVE</span>}
								</div>
								<div className="flex-1 text-right tabular-nums font-medium">
									{row.amount} <span className="text-[#888] text-[11px]">ETH</span>
								</div>
								<div className="flex-1 text-right truncate" title={row.nickname}>
									{row.nickname}
								</div>
							</div>
						);
					})}
				</div>
				<p className="mt-2 text-[#555] text-xs">최신 {maxRows}개 (실시간 포함). 실시간 행은 LIVE 표시.</p>
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
			</div>
		</div>
	);
}
