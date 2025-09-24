/* eslint-disable no-undef */
/**
 * Firebase Messaging Service Worker
 * - 백엔드에서 WebpushConfig + Notification payload 를 내려주는 구조를 지원
 * - data-only 메시지, silent 메시지, rich 메시지 모두 처리 가능
 * - 아이콘/뱃지 경로는 환경/배포에 맞게 조정
 */

importScripts("https://www.gstatic.com/firebasejs/10.12.2/firebase-app-compat.js");
importScripts("https://www.gstatic.com/firebasejs/10.12.2/firebase-messaging-compat.js");

// ================== 구성 ==================
const SW_VERSION = "v1.0.0"; // 버전 변경 시 새 SW 강제 업데이트
const DEBUG = true; // 필요시 false
const DEFAULT_ICON = "/icon/fcm_icon_192.png";
const DEFAULT_BADGE = "/icon/fcm_icon_72.png";
const DEFAULT_FALLBACK_TITLE = "알림";
const DEFAULT_CLICK_FALLBACK = "/";

/**
 * 빌드 시 치환될 Firebase 설정 placeholder
 */
const cfg = {
	apiKey: "@@FIREBASE_API_KEY@@",
	authDomain: "@@FIREBASE_AUTH_DOMAIN@@",
	projectId: "@@FIREBASE_PROJECT_ID@@",
	storageBucket: "@@FIREBASE_STORAGE_BUCKET@@",
	messagingSenderId: "@@FIREBASE_MESSAGING_SENDER_ID@@",
	appId: "@@FIREBASE_APP_ID@@",
	measurementId: "@@FIREBASE_MEASUREMENT_ID@@",
};

firebase.initializeApp(cfg);
const messaging = firebase.messaging();

// ================== 유틸 ==================

function log(...args) {
	if (DEBUG) console.log("[SW][FCM]", ...args);
}

/**
 * 기존 클라이언트(탭 / PWA 창) 포커스 또는 새 창 오픈
 */
async function openOrFocusClient(url) {
	const allClients = await clients.matchAll({
		type: "window",
		includeUncontrolled: true,
	});
	const target = new URL(url, self.location.origin).href;

	for (const client of allClients) {
		try {
			const cUrl = new URL(client.url);
			// 동일 origin 이고 루트 혹은 target과 시작 경로 일치 시 포커스
			if (cUrl.origin === self.location.origin) {
				// 특정 라우팅 전략 필요하면 여기서 client.postMessage 가능
				await client.focus();
				// 필요 시 SPA 라우팅을 위해 message
				client.postMessage({ type: "FCM_CLICK", target });
				return;
			}
		} catch (e) {
			// ignore
		}
	}
	// 열려있는 창이 없으면 새 창
	await clients.openWindow(target);
}

/**
 * Notification Options 생성 (payload 기반 재조합)
 */
function buildNotificationOptions(payload, forceOverride = false) {
	const { notification, data = {} } = payload || {};
	// 백엔드 WebpushNotification 에 있는 customData(click_action) 는 data 로 merge 된다고 가정
	const title = data.title || data.notificationTitle || notification?.title || DEFAULT_FALLBACK_TITLE;

	const body = (data.body || data.notificationBody || notification?.body || "") + "";

	// icon / badge 우선순위: data -> notification -> default
	const icon = data.icon || notification?.icon || DEFAULT_ICON;

	const badge = data.badge || notification?.badge || DEFAULT_BADGE;

	// click_action (경로 or 절대 URL)
	const clickAction = data.click_action || data.clickAction || notification?.click_action || DEFAULT_CLICK_FALLBACK;

	// silent 메시지 판단(표시 안 함): type === SILENT 등 규칙 가능
	const silent = data.silent === "true" || data.type === "SILENT";

	const tag = data.tag || `fcm-${data.type || "generic"}`;
	const renotify = data.renotify === "true";

	const actions = [];
	// 액션 예시 (백엔드 data 로 제어 가능)
	// if (data.action_mark_read) {
	//   actions.push({ action: "mark_read", title: "읽음", icon: "/icon/action_check.png" });
	// }

	const requireInteraction = data.requireInteraction === "true"; // 사용자가 직접 닫을 때까지 유지

	const options = {
		body,
		icon,
		badge,
		tag,
		renotify,
		data: {
			...data,
			click_action: clickAction,
			_swVersion: SW_VERSION,
			_originHasNotificationPayload: !!notification,
		},
		actions,
		requireInteraction,
	};

	return { title, options, silent, forceOverride };
}

// ================== 수신 처리 ==================

/**
 * FCM SDK가 전달하는 background 메시지 처리.
 * - payload.notification 이 있으면 브라우저 기본 표시가 “이미” 되므로
 *   우리가 다시 표시할지 여부를 선택.
 */
messaging.onBackgroundMessage((payload) => {
	log("onBackgroundMessage raw payload:", payload);

	// 기본 정책:
	// 1) payload.notification 이 있고, 특별한 data.type (예:RICH, FORCE_OVERRIDE)가 아니면 그대로 둔다 (return)
	// 2) data.type 이 RICH 이거나 alwaysOverride 가 true 면 재표시
	const data = payload.data || {};
	const hasNotification = !!payload.notification;
	const richType = data.type === "RICH";
	const forceOverride = data.alwaysOverride === "true";

	if (hasNotification && !richType && !forceOverride) {
		log("Notification payload already displayed by browser. Skipping re-display.");
		return;
	}

	const { title, options, silent } = buildNotificationOptions(payload, forceOverride);

	if (silent) {
		log("Silent message received (no notification shown).");
		// 필요 시 여기서 IndexedDB 갱신, sync, postMessage 등
		return;
	}

	self.registration.showNotification(title, options);
});

// ================== 이벤트: 클릭 ==================

self.addEventListener("notificationclick", (event) => {
	event.notification.close();
	const clickAction = event.notification?.data?.click_action || DEFAULT_CLICK_FALLBACK;

	// 액션 버튼 구분
	if (event.action) {
		switch (event.action) {
			case "mark_read":
				// 선택적으로 서버에 읽음 처리 POST
				event.waitUntil(
					(async () => {
						try {
							await fetch("/api/notifications/mark-read", {
								method: "POST",
								headers: { "Content-Type": "application/json" },
								body: JSON.stringify({ id: event.notification?.data?.id }),
							});
						} catch (e) {
							log("mark_read failed", e);
						}
						await openOrFocusClient(clickAction);
					})()
				);
				return;
			default:
				// 기타 커스텀 액션
				break;
		}
	}

	event.waitUntil(openOrFocusClient(clickAction));
});

// ================== 이벤트: 닫힘 ==================

self.addEventListener("notificationclose", (event) => {
	// 사용자가 닫았을 때 분석/기록 필요하면 여기서 서버 전송
	if (DEBUG) {
		log("Notification closed", event.notification?.data);
	}
});

// ================== 메시지(클라이언트 <-> SW) 통신 (선택) ==================

self.addEventListener("message", (event) => {
	if (!event.data) return;
	if (event.data.type === "PING") {
		event.ports?.[0]?.postMessage({ type: "PONG", version: SW_VERSION });
	}
});

// ================== 설치/활성화 (버전 관리) ==================

self.addEventListener("install", (event) => {
	log("SW installing...", SW_VERSION);
	self.skipWaiting();
});

self.addEventListener("activate", (event) => {
	log("SW activating...", SW_VERSION);
	// 오래된 알림 정리 로직을 넣고 싶다면 여기 가능
	event.waitUntil(
		(async () => {
			const notis = await self.registration.getNotifications();
			// 예: 특정 tag 패턴 정리
			// for (const n of notis) { if (n.tag.startsWith('fcm-legacy-')) n.close(); }
			log("Active with existing notifications:", notis.length);
			await clients.claim();
		})()
	);
});
