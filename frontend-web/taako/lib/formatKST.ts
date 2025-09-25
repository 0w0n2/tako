// lib/formatKST.ts
// 기존 구현은 new Date(iso) (이미 로컬타임대입 = KST 환경이면 +9 반영된 값) 에 +9시간을 다시 더해 +18이 되는 문제가 있었음.
// 해결: Intl.DateTimeFormat(timeZone: 'Asia/Seoul') 을 활용해 어떤 클라이언트 타임존이든 정확히 KST 한 번만 적용.

// 공통 formatter (Full 형식에서 formatToParts 사용하므로 별도)
const KST_COMPACT_FMT = new Intl.DateTimeFormat("ko-KR", {
	year: "numeric",
	month: "2-digit",
	day: "2-digit",
	hour: "2-digit",
	minute: "2-digit",
	second: "2-digit",
	hour12: false,
	timeZone: "Asia/Seoul",
});

// 백엔드가 ISO8601 (끝에 'Z' 또는 +hh:mm) 형태를 보낼 때는 Date가 UTC 인스턴트를 생성한다.
// 하지만 "YYYY-MM-DD HH:mm:ss" 처럼 타임존이 없는 경우 브라우저는 이를 로컬 시간으로 해석한다.
// 우리의 규칙: "타임존 표기가 전혀 없는 19자리(초까지) 형태" 는 'UTC' 로 간주한 뒤 KST 표시.
// 예: 2025-09-25 03:10:05  -> 실제 UTC 03:10:05 로 취급 -> Asia/Seoul 표시 시 12:10:05
const NO_TZ_DATETIME = /^(\d{4})-(\d{2})-(\d{2})[ T](\d{2}):(\d{2}):(\d{2})$/;
function safeParse(utcIso?: string | null): Date | null {
	if (!utcIso) return null;
	const trimmed = utcIso.trim();
	let dateObj: Date | null = null;

	// 타임존(+hh:mm 또는 Z) 패턴이 끝에 없는지 검사하여 로컬 시간 파싱을 방지
	if (NO_TZ_DATETIME.test(trimmed) && !/(?:[zZ]|[+-]\d{2}:?\d{2})$/.test(trimmed)) {
		// 명시적 타임존 없고 패턴 일치 -> UTC 가정
		const m = NO_TZ_DATETIME.exec(trimmed);
		if (m) {
			const [_, y, mo, d, hh, mm, ss] = m;
			// Date.UTC(year, monthIndex, day, hour, minute, second)
			const ts = Date.UTC(Number(y), Number(mo) - 1, Number(d), Number(hh), Number(mm), Number(ss));
			if (Number.isFinite(ts)) {
				dateObj = new Date(ts);
			}
		}
	}

	if (!dateObj) {
		const d = new Date(trimmed);
		if (!isNaN(d.getTime())) dateObj = d;
		else return null;
	}
	return dateObj;
}

/**
 * 'YYYY년 MM월 DD일 HH시 mm분 ss초' (KST) 반환
 */
export function formatKSTFull(utcIso?: string | null): string {
	const d = safeParse(utcIso);
	if (!d) return utcIso ? String(utcIso) : "-";
	// parts 추출 (연/월/일/시/분/초)
	const parts = new Intl.DateTimeFormat("ko-KR", {
		year: "numeric",
		month: "2-digit",
		day: "2-digit",
		hour: "2-digit",
		minute: "2-digit",
		second: "2-digit",
		hour12: false,
		timeZone: "Asia/Seoul",
	}).formatToParts(d);
	const get = (type: string) => parts.find((p) => p.type === type)?.value ?? "";
	return `${get("year")}년 ${get("month")}월 ${get("day")}일 ${get("hour")}시 ${get("minute")}분 ${get("second")}초`;
}

/** 짧은 표기: MM-DD HH:mm:ss (KST) */
export function formatKSTCompact(utcIso?: string | null): string {
	const d = safeParse(utcIso);
	if (!d) return utcIso ? String(utcIso) : "-";
	const parts = KST_COMPACT_FMT.formatToParts(d);
	const get = (type: string) => parts.find((p) => p.type === type)?.value ?? "";
	return `${get("month")}-${get("day")} ${get("hour")}:${get("minute")}:${get("second")}`;
}

/** Date 객체 혹은 타임스탬프도 처리 (UTC 인스턴트로 간주하여 KST로 표시) */
export function formatKSTFromDate(date: Date | number): string {
	const ts = date instanceof Date ? date.getTime() : date;
	if (!Number.isFinite(ts)) return "-";
	const d = new Date(ts);
	const parts = KST_COMPACT_FMT.formatToParts(d);
	const get = (type: string) => parts.find((p) => p.type === type)?.value ?? "";
	return `${parts.find((p) => p.type === "year")?.value}-${get("month")}-${get("day")} ${get("hour")}:${get("minute")}:${get("second")}`;
}

/** 유닉스 초(second) 값이 오는 경우 도움 함수 */
export function formatKSTFromUnixSeconds(sec?: number): string {
	if (sec == null || !Number.isFinite(sec)) return "-";
	return formatKSTFromDate(sec * 1000);
}

/** KST Date 객체가 필요할 때 (표시가 아니라 계산용) */
export function toKstDate(utcIso?: string | null): Date | null {
	const d = safeParse(utcIso);
	if (!d) return null;
	// KST 시각의 "구성요소"를 얻어 새 Date를 만들 필요가 있을 때는 getUTC* + 9 를 사용해야 하지만
	// 계산(remaining 등)은 그냥 UTC timestamp 기반으로 하는 편이 안전. 여기서는 단순히 원래 인스턴트 반환.
	// 별도 KST 보정된 Date 객체가 꼭 필요하면 아래처럼 변환 가능 (주석 참고):
	// const parts = formatKSTFull(utcIso); // 문자열 -> 다시 파싱 비효율
	return d;
}

export default {
	formatKSTFull,
	formatKSTCompact,
	formatKSTFromDate,
	formatKSTFromUnixSeconds,
	toKstDate,
};
