import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import { fetchFcmStatus, getOrCreateFcmToken, registerTokenToBackend, requestNotificationPermission, removeFcmToken, resetSingleDevice } from "@/lib/fcm";

const DEBUG_FCM = process.env.NEXT_PUBLIC_DEBUG_FCM === "1";
const slog = (...args: any[]) => {
	if (DEBUG_FCM) console.log("[PushStore]", ...args);
};

interface PushState {
	token: string | null;
	enabled: boolean; // 현재 브라우저 토큰 등록 여부
	anyRegistered: boolean; // 회원에게 하나라도 등록된 토큰 존재
	loading: boolean;
	error: string | null;
	initialized: boolean;
	// AuthenticationPrincipal 기반으로 서버가 사용자 식별 → 매개변수 제거
	checkStatus: () => Promise<void>;
	enable: () => Promise<void>;
	disable: () => Promise<void>;
}

export const usePushStore = create<PushState>()(
	persist(
		(set, get) => ({
			token: null,
			enabled: false,
			anyRegistered: false,
			loading: false,
			error: null,
			initialized: false,

			checkStatus: async () => {
				slog("checkStatus:start");
				set({ loading: true });
				try {
					const currentToken = get().token || undefined;
					slog("checkStatus:currentToken", currentToken?.substring(0, 12));
					const status = await fetchFcmStatus(currentToken);
					slog("checkStatus:status", status);
					set({
						enabled: status.currentRegistered,
						anyRegistered: status.hasAnyToken,
						loading: false,
						initialized: true,
					});
				} catch (e: any) {
					slog("checkStatus:error", e);
					set({ error: e.message || "FCM 상태 확인 실패", loading: false });
				}
			},

			enable: async () => {
				slog("enable:start");
				set({ loading: true, error: null });
				try {
					const permission = await requestNotificationPermission();
					slog("enable:permission", permission);
					if (permission !== "granted") {
						set({ loading: false, error: "알림 권한이 거부되었습니다." });
						return;
					}
					const token = await getOrCreateFcmToken();
					slog("enable:token", token?.substring(0, 25));
					if (!token) throw new Error("토큰 생성 실패");
					await registerTokenToBackend(token);
					slog("enable:backend registered");
					set({ token, enabled: true, anyRegistered: true, loading: false });
					slog("enable:done");
				} catch (e: any) {
					slog("enable:error", e);
					set({ error: e.message || "FCM 활성화 실패", loading: false });
				}
			},

			disable: async () => {
				slog("disable:start");
				set({ loading: true, error: null });
				try {
					const token = get().token;
					slog("disable:current token", token?.substring(0, 25));
					if (token) {
						try {
							await resetSingleDevice(token);
							slog("disable:backend resetSingleDevice done");
						} catch (e) {
							slog("disable:backend resetSingleDevice error (continue)", e);
						}
					}
					await removeFcmToken(token || undefined);
					set({ token: null, enabled: false, loading: false });
					slog("disable:done");
				} catch (e: any) {
					slog("disable:error", e);
					set({ error: e.message || "FCM 비활성화 실패", loading: false });
				}
			},
		}),
		{
			name: "pushStorage",
			storage: createJSONStorage(() => localStorage),
			partialize: (s) => ({ token: s.token }),
		}
	)
);
