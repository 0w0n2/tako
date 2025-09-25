import { useState, useEffect } from "react";

interface Props {
	readonly start?: string; // ISO
	readonly end?: string; // ISO
	readonly endTsOverride?: number; // SSE로 갱신되는 epoch(ms) or seconds
	readonly className?: string;
}

export default function RemainingTime({ start, end, endTsOverride, className }: Props) {
	const [now, setNow] = useState<number>(Date.now());

	useEffect(() => {
		const t = setInterval(() => setNow(Date.now()), 1000);
		return () => clearInterval(t);
	}, []);

	// endTsOverride가 초 단위면 ms로 보정
	const endMs = (() => {
		if (typeof endTsOverride === "number") {
			return endTsOverride > 1e12 ? endTsOverride : endTsOverride * 1000;
		}
		if (end) return new Date(end).getTime();
		return undefined;
	})();

	const startMs = start ? new Date(start).getTime() : undefined;

	let phase: "before" | "running" | "ended" = "running";
	if (startMs && now < startMs) phase = "before";
	if (endMs && now >= endMs) phase = "ended";

	const diff = endMs ? endMs - now : 0;
	const remain = diff > 0 ? diff : 0;
	const totalSeconds = Math.floor(remain / 1000);
	const days = Math.floor(totalSeconds / 86400);
	const hours = Math.floor((totalSeconds % 86400) / 3600);
	const minutes = Math.floor((totalSeconds % 3600) / 60);
	const seconds = totalSeconds % 60;

	const segs: string[] = [];
	if (days > 0) segs.push(`${days}일`);
	if (hours > 0) segs.push(`${hours}시간`);
	if (minutes > 0) segs.push(`${minutes}분`);
	segs.push(`${seconds}초`);

	let text: string;
	if (phase === "before") text = `시작 전 (${segs.join(" ")})`;
	else if (phase === "ended") text = "종료됨";
	else text = segs.join(" ");

	return <span className={className ?? "text-[#7db7cd]"}>{text}</span>;
}
