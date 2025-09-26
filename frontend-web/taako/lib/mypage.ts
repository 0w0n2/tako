import api from "@/lib/api";
import type { MyBidAuctions, MyInfo } from "@/types/auth";

// ================= 새 프로필 관련 타입 =================
export interface MyProfileResult {
	memberId: number;
	email: string;
	nickname: string;
	introduction: string;
	profileImageUrl: string | null;
	backgroundImageUrl: string | null;
	walletAddress: string | null;
}

interface ApiBase<T> {
	httpStatus: any; // 백엔드에서 쓰는 스키마 그대로 두되 사용 안함
	isSuccess: boolean;
	message: string;
	code: number;
	result: T;
}

// GET /v1/members/me (내 프로필 조회)
export async function fetchMyProfile(): Promise<MyProfileResult> {
	const res = await api.get<ApiBase<MyProfileResult>>("/v1/members/me");
	return res.data.result;
}

// GET /v1/auth/availability/nickname?nickname=xxx
export async function checkNicknameAvailable(nickname: string) {
	if (!nickname) throw new Error("닉네임을 입력하세요.");
	const res = await api.get<ApiBase<{ field: string; value: string; available: boolean }>>("/v1/auth/availability/nickname", { params: { nickname } });
	return res.data.result;
}

// PATCH /v1/members/me - form-data (nickname, introduction, profileImage, backgroundImage)
export interface PatchProfilePayload {
	nickname?: string;
	introduction?: string;
	profileImageFile?: File | null;
	backgroundImageFile?: File | null;
}

export async function patchMyProfile(payload: PatchProfilePayload) {
	const form = new FormData();
	if (payload.nickname !== undefined) form.append("nickname", payload.nickname);
	if (payload.introduction !== undefined) form.append("introduction", payload.introduction);
	if (payload.profileImageFile) form.append("profileImage", payload.profileImageFile);
	if (payload.backgroundImageFile) form.append("backgroundImage", payload.backgroundImageFile);

	const res = await api.patch<ApiBase<Record<string, never>>>("/v1/members/me", form, {
		headers: { "Content-Type": "multipart/form-data" },
	});
	return res.data;
}

type Page<T> = { content: T[]; page: number; size: number; totalElements: number; totalPages: number };

type BidFilter =
	| { ended: boolean; page?: number; size?: number } // ✔ /v1/mypage/bids?ended=true|false
	| { isEnd: boolean; page?: number; size?: number } // ✔ ...?isEnd=true|false
	| { status: "ONGOING" | "ENDED"; page?: number; size?: number } // ✔ ...?status=...
	| { page?: number; size?: number }; // 기본

export async function getMyBidAuction(opts: BidFilter = {}): Promise<Page<MyBidAuctions>> {
	const { page = 0, size = 10 } = opts as any;
	const params: Record<string, any> = { page, size };

	// 우선순위: ended -> isEnd -> status
	if ("ended" in opts) params.ended = opts.ended;
	else if ("isEnd" in opts) params.isEnd = opts.isEnd;
	else if ("status" in opts) params.status = opts.status;

	const res = await api.get("/v1/auctions/mybid", { params });
	return res.data.result as Page<MyBidAuctions>;
}

export async function getInfo() {
	const res = await api.get("/v1/members/me");
	return res.data.result;
}

// 내 판매 경매 조회
export const getMySellAutcion = async () => {
	const res = await api.get("/v1/auctions/me");
	return res.data;
};

type ApiEnvelope<T> = {
  httpStatus: Record<string, unknown>;
  isSuccess: boolean;
  message: string;
  code: number;
  result: T;
};

export async function getInfoMe(): Promise<MyInfo> {
  const res = await api.get<ApiEnvelope<MyInfo>>("/v1/members/me");
  return res.data.result;
}