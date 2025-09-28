import { useCallback, useEffect, useMemo, useState } from "react";
import { getCardGradeDailyStats } from "@/lib/card";

export interface GradeDailyPoint {
	date: string;
	amountMax: number | null;
	amountAvg: number | null;
	amountMin: number | null;
}

export interface GradeDailySeries {
	grade: string;
	items: GradeDailyPoint[];
}

export interface UseCardGradeDailyStatsState {
	loading: boolean;
	error: string | null;
	series: GradeDailySeries[];
	grades: string[];
	reload: (days?: number) => Promise<void>;
}

export function useCardGradeDailyStats(cardId: number | string, days = 7): UseCardGradeDailyStatsState {
	const [loading, setLoading] = useState(false);
	const [error, setError] = useState<string | null>(null);
	const [series, setSeries] = useState<GradeDailySeries[]>([]);

	const fetcher = useCallback(
		async (d?: number) => {
			if (!cardId) return;
			setLoading(true);
			setError(null);
			try {
				const res = await getCardGradeDailyStats(cardId, { days: d ?? days });
				const list: GradeDailySeries[] = Array.isArray(res) ? res : res?.result ?? [];
				// 보정: items는 date ASC로 유지
				const normalized = (list ?? []).map((s) => ({
					grade: s.grade,
					items: [...(s.items ?? [])].sort((a, b) => {
						if (a.date < b.date) return -1;
						if (a.date > b.date) return 1;
						return 0;
					}),
				}));
				setSeries(normalized);
			} catch (e: any) {
				setError(e?.message ?? "시세 통계를 불러오지 못했습니다.");
			} finally {
				setLoading(false);
			}
		},
		[cardId, days]
	);

	useEffect(() => {
		fetcher(days);
	}, [fetcher, days]);

	const grades = useMemo(() => series.map((s) => s.grade), [series]);

	return useMemo(() => ({ loading, error, series, grades, reload: fetcher }), [loading, error, series, grades, fetcher]);
}
