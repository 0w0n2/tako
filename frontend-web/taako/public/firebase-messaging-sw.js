/* eslint-disable no-undef */
// Firebase Messaging Service Worker
// 별도 번들없이 CDN import (Next.js public service worker)

importScripts("https://www.gstatic.com/firebasejs/10.12.2/firebase-app-compat.js");
importScripts("https://www.gstatic.com/firebasejs/10.12.2/firebase-messaging-compat.js");

// Note: service worker에서는 Next.js 런타임 env 주입(self.env)이 기본적으로 존재하지 않습니다.
// 빌드/배포 파이프라인에서 템플릿 치환을 하거나, 아래에 직접 값을 인라인해야 합니다.
// (apiKey 등은 공개되어도 무방한 Web FCM 공개 키)
const cfg = {
	apiKey: self.env && self.env.NEXT_PUBLIC_FIREBASE_API_KEY, // 배포 시 치환 필요
	authDomain: self.env && self.env.NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN,
	projectId: self.env && self.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID,
	storageBucket: self.env && self.env.NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET,
	messagingSenderId: self.env && self.env.NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID,
	appId: self.env && self.env.NEXT_PUBLIC_FIREBASE_APP_ID,
	measurementId: self.env && self.env.NEXT_PUBLIC_FIREBASE_MEASUREMENT_ID,
};

firebase.initializeApp(cfg);

const messaging = firebase.messaging();

messaging.onBackgroundMessage(function (payload) {
	console.log("[firebase-messaging-sw.js] Received background message ", payload);
	const notificationTitle = payload.notification?.title || "알림";
	const notificationOptions = {
		body: payload.notification?.body || "",
		icon: "/logo.png",
		data: payload.data || {},
	};

	self.registration.showNotification(notificationTitle, notificationOptions);
});

self.addEventListener("notificationclick", function (event) {
	event.notification.close();
	const targetUrl = event.notification?.data?.click_action || "/";
	event.waitUntil(clients.openWindow(targetUrl));
});
