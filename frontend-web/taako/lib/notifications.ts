import api from "@/lib/api";
import { BaseEnvelope, PageResponse, NotificationListRow, UnreadCountResponse } from "@/types/notificationsPage";

const BASE = "http://localhost:8080";

export async function fetchNotifications(page = 0, size = 20) {
	const { data } = await api.get<BaseEnvelope<PageResponse<NotificationListRow>>>(`${BASE}/v1/notifications?page=${page}&size=${size}`);
	return data.result;
}

export async function fetchUnreadCount() {
	const { data } = await api.get<BaseEnvelope<UnreadCountResponse>>(`${BASE}/v1/notifications/unread-count`);
	return data.result.unreadCount;
}

export async function markNotificationRead(id: number) {
	await api.patch(`${BASE}/v1/notifications/${id}/read`);
	return true;
}

export async function markAllNotificationsRead() {
	const { data } = await api.post<BaseEnvelope<number>>(`${BASE}/v1/notifications/read-all`);
	return data.result; // changed count
}
