import api from "@/lib/api";
import { NotificationSettingsResponse, PatchNotificationSettingsRequest, NotificationSettingsMap } from "@/types/notification";

const BASE = "http://localhost:8080"; // 향후 env 로 추출 가능

export async function fetchNotificationSettings(): Promise<NotificationSettingsMap> {
	const { data } = await api.get<NotificationSettingsResponse>(`${BASE}/v1/members/me/notification-settings`);
	return data.result;
}

export async function patchNotificationSettings(partial: PatchNotificationSettingsRequest["notificationSetting"]): Promise<NotificationSettingsMap> {
	const { data } = await api.patch<NotificationSettingsResponse>(`${BASE}/v1/members/me/notification-settings`, { notificationSetting: partial });
	return data.result;
}
