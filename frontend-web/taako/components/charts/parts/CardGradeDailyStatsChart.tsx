"use client";

import { useMemo, useState } from "react";
import { AreaChart, Area, Line, XAxis, YAxis, Tooltip, CartesianGrid, ResponsiveContainer } from "recharts";
import { useCardGradeDailyStats } from "@/hooks/useCardGradeDailyStats";
import RankElement from "@/components/atoms/RankElement";

interface Props {
	cardId: number | string;
	days?: number; // default 7
	className?: string;
}

function formatValue(v: number | null | undefined) {
	if (v == null) return "-";
	return v;
}

// 날짜 라벨을 월/일(M/D)로 포맷
function formatDateLabel(input: unknown): string {
	// 숫자 타임스탬프
	if (typeof input === "number") {
		const dt = new Date(input);
		if (!isNaN(dt.getTime())) return `${dt.getMonth() + 1}/${dt.getDate()}`;
	}
	// Date 객체
	if (input instanceof Date) {
		const dt = input;
		if (!isNaN(dt.getTime())) return `${dt.getMonth() + 1}/${dt.getDate()}`;
	}
	// 문자열(ISO 또는 yyyy-mm-dd)
	if (typeof input === "string") {
		const s = input;
		if (/^\d{4}-\d{2}-\d{2}/.test(s)) {
			const [, m, d] = s.split("T")[0].split("-");
			return `${Number(m)}/${Number(d)}`;
		}
		const dt = new Date(s);
		if (!isNaN(dt.getTime())) return `${dt.getMonth() + 1}/${dt.getDate()}`;
		return s;
	}
	return "";
}

function CustomTooltip({ active, payload, label }: any) {
	if (!active || !payload?.length) return null;
	// range(=max-min)는 UI에서 숨기고, min/max/avg만 노출
	const allowed = new Set(["min", "max", "avg"]);
	const rows = payload.filter((p: any) => p.value != null && allowed.has(p.dataKey));
	if (!rows.length) return null;
	return (
		<div className="bg-gray-900 text-white p-3 rounded shadow-lg min-w-[160px]">
			<p className="font-semibold mb-1">{formatDateLabel(label)}</p>
			{rows.map((entry: any) => (
				<p key={entry.dataKey} className={`text-md mb-1`}>
					{entry.name}: {formatValue(entry.value)}
				</p>
			))}
		</div>
	);
}

export default function CardGradeDailyStatsChart({ cardId, days = 7, className }: Readonly<Props>) {
	const { loading, error, series, grades } = useCardGradeDailyStats(cardId, days);
	const [activeGrade, setActiveGrade] = useState<string | null>(null);

	// 표시 순서를 고정: S+ S A B C D, 그 외 등급은 뒤에 유지
	const displayGrades = useMemo(() => {
		const order = ["S+", "S", "A", "B", "C", "D"] as const;
		const ordered = order.filter((g) => grades.includes(g as unknown as string));
		const rest = grades.filter((g) => !ordered.includes(g as any));
		return [...ordered, ...rest];
	}, [grades]);

	const currentGrade = useMemo(() => {
		return activeGrade && displayGrades.includes(activeGrade) ? activeGrade : displayGrades[0];
	}, [activeGrade, displayGrades]);

	const target = useMemo(() => {
		return series.find((s) => s.grade === currentGrade);
	}, [series, currentGrade]);

	const data = useMemo(() => {
		const items = target?.items ?? [];
		// Recharts에서 connectNulls로 null을 이어 그리게 함
		return items.map((d) => ({
			date: d.date,
			max: d.amountMax,
			avg: d.amountAvg,
			min: d.amountMin,
			minBase: d.amountMin, // 밴드 베이스(툴팁 제외용)
			// 밴드 표현(range = max - min)
			range: d.amountMax != null && d.amountMin != null ? d.amountMax - d.amountMin : null,
		}));
	}, [target]);

	return (
		<div className={className}>
			{/* Grade 선택 탭 */}
			<div className="mb-6 w-full flex items-center justify-between">
				{displayGrades.map((g) => {
					const active = (activeGrade ?? displayGrades[0]) === g;
					return (
						<button
							key={g}
							type="button"
							onClick={() => setActiveGrade(g)}
							aria-pressed={active}
							title={`${g} 등급`}
							className={`rounded-lg transition-transform focus:outline-none focus:ring-2 focus:ring-sky-400 ${active ? "ring-2 ring-sky-400 scale-[1.02]" : "hover:scale-[1.02]"}`}
						>
							<RankElement rank={g} />
						</button>
					);
				})}
			</div>

			{error && <p className="text-red-400 text-sm mb-2">{error}</p>}

			<div className="w-full h-[240px] rounded-lg border border-[#353535] flex items-center justify-center">
				{(!data || data.length === 0) && !loading ? (
					<p className="text-gray-400 text-sm">최근 {days}일 데이터가 없습니다.</p>
				) : (
					<ResponsiveContainer width="100%" height="100%">
						<AreaChart data={data} margin={{ left: 8, right: 8, top: 10, bottom: 0 }}>
							<CartesianGrid stroke="#222" strokeDasharray="3 3" />
							<XAxis dataKey="date" tick={{ fill: "#aaa" }} axisLine={false} tickFormatter={formatDateLabel} />
							<YAxis tick={{ fill: "#aaa" }} axisLine={false} />
							<Tooltip content={<CustomTooltip />} />

							{/* 밴드: minBase(투명)를 베이스로 두고, range(=max-min)를 파란색 반투명으로 채워서 min~max 사이만 색칠 */}
							<Area type="monotone" dataKey="minBase" stroke="transparent" fillOpacity={0} connectNulls stackId="range" />
							<Area type="monotone" dataKey="range" name="범위" stroke="transparent" fill="#3b82f6" fillOpacity={0.2} connectNulls stackId="range" />

							{/* 최고가/최저가 라인 색상 지정 */}
							<Line type="monotone" dataKey="max" name="최대가" stroke="#ef4444" strokeWidth={2} dot={false} connectNulls />
							<Line type="monotone" dataKey="min" name="최저가" stroke="#22c55e" strokeWidth={2} dot={false} connectNulls />

							{/* 평균 라인 */}
							<Line type="monotone" dataKey="avg" name="평균가" stroke="#93c5fd" strokeWidth={2} dot={false} connectNulls />
						</AreaChart>
					</ResponsiveContainer>
				)}
			</div>
		</div>
	);
}
