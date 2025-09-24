import api from "@/lib/api";
import { getFirebaseMessaging } from "@/lib/firebase";
import { getToken, deleteToken, onMessage } from "firebase/messaging";

// 디버그 플래그 (환경변수로 on/off)
const DEBUG_FCM = process.env.NEXT_PUBLIC_DEBUG_FCM === "1";
const log = (...args: any[]) => {
	if (DEBUG_FCM) console.log("[FCM]", ...args);
};

// 백엔드 추정 응답 타입 (status 엔드포인트)
export interface FcmStatus {
	hasAnyToken: boolean; // 회원이 하나라도 등록된 FCM 토큰이 있는지
	currentRegistered: boolean; // 현재 브라우저 토큰이 등록되어 있는지
	tokenCount: number; // 등록된 총 토큰 수
}

const VAPID_KEY = process.env.NEXT_PUBLIC_FIREBASE_VAPID_KEY;
log("VAPID_KEY loaded?", !!VAPID_KEY);

export async function requestNotificationPermission(): Promise<NotificationPermission> {
	if (typeof window === "undefined") return "denied";
	log("Requesting notification permission...");
	const permission = await Notification.requestPermission();
	log("Permission result:", permission);
	return permission;
}

export async function getOrCreateFcmToken(): Promise<string | null> {
	const messaging = await getFirebaseMessaging();
	if (!messaging) {
		log("Messaging not available");
		return null;
	}
	try {
		log("Calling getToken with vapidKey length:", VAPID_KEY?.length);
		const token = await getToken(messaging, { vapidKey: VAPID_KEY });
		log("Received token:", token ? token.substring(0, 12) + "..." : null);
		return token || null;
	} catch (e) {
		console.error("[FCM] getToken error", e);
		return null;
	}
}

export async function removeFcmToken(token?: string): Promise<boolean> {
	const messaging = await getFirebaseMessaging();
	if (!messaging) {
		log("removeFcmToken: messaging null");
		return false;
	}
	try {
		log("Deleting token. Provided?", !!token);
		const result = await deleteToken(messaging);
		log("deleteToken result:", result);
		return result;
	} catch (e) {
		console.error("[FCM] deleteToken error", e);
		return false;
	}
}

// 인증 기반: 서버가 AuthenticationPrincipal 로 사용자 식별
export async function registerTokenToBackend(token: string) {
	log("registerTokenToBackend start", { tokenPreview: token.substring(0, 10) });
	await api.post(`http://localhost:8080/v1/fcm/token`, { token });
	log("registerTokenToBackend done");
}

export async function resetSingleDevice(token: string) {
	log("resetSingleDevice");
	await api.post(`http://localhost:8080/v1/fcm/token/reset`, { token });
}

export async function fetchFcmStatus(currentToken?: string): Promise<FcmStatus> {
	const query = currentToken ? `?currentToken=${encodeURIComponent(currentToken)}` : "";
	log("fetchFcmStatus ->", query || "(no token param)");
	const res = await api.get(`http://localhost:8080/v1/fcm/status${query}`);
	log("fetchFcmStatus raw response", res.data);
	// 백엔드가 { active, currentRegistered, tokenCount } 형태라고 로그 상 추정 → hasAnyToken 에 active 매핑
	const data = res.data;
	const normalized: FcmStatus = {
		hasAnyToken: typeof data.active === "boolean" ? data.active : !!data.hasAnyToken,
		currentRegistered: !!data.currentRegistered,
		tokenCount: typeof data.tokenCount === "number" ? data.tokenCount : data.count || 0,
	};
	log("fetchFcmStatus normalized", normalized);
	return normalized;
}

export function subscribeForegroundMessage(callback: (payload: unknown) => void) {
	getFirebaseMessaging().then((m) => {
		if (!m) {
			log("subscribeForegroundMessage: messaging null");
			return;
		}
		onMessage(m, (payload: any) => {
			log("foreground message", payload);
			callback(payload);
		});
	});
}
