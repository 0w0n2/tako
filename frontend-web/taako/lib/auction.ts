import api from "./api";
import { GetHotCards, AuctionDetailProps, WeeklyAuctions } from "@/types/auction";
import type { Card } from '@/types/card';
import type { Seller } from '@/types/seller';
import type { History } from '@/types/history';

// 경매 등록
export const createAuction = async(requestDto:any, files: File[]) => {
    const formData = new FormData();
    // requestDto는 JSON형식, files는 파일형식으로 request
    formData.append("requestDto", JSON.stringify(requestDto));
    files.forEach(file => {
        formData.append("files", file);
    });

    const res = await api.post("/v1/auctions", formData,{
        headers: {
            "Content-Type": "multipart/form-data", // 이거 필수
        },
    })
    return res.data;
}

// 경매 목록 조회
export const getAuctions = async(params: GetHotCards) => {
    const res = await api.get("/v1/auctions", {
        params: params
    })
    return res.data;
}

// 경매 상세 페이지
type RawResponse = {
  httpStatus: string;
  isSuccess: boolean;
  message: string;
  code: number;
  result: {
    auction: any;
    card: any;
    imageUrls: string[];
    history: any[];
    weeklyPrices: any[];
    seller: any;
    wished: boolean;
  };
};

function normalizeCard(raw: any): Card {
  return {
    id: raw?.id ?? 0,
    name: raw?.cardName ?? raw?.name ?? '',
    grade: raw?.grade ?? '',            // 등급은 auction에 있을 수도 있어 아래에서 덮어씀
    rarity: raw?.rarity ?? 'DEFAULT',
    categoryMajorId: raw?.categoryMajorId ?? 0,
    categoryMajorName: raw?.categoryMajorName ?? '',
    categoryMediumId: raw?.categoryMediumId ?? 0,
    categoryMediumName: raw?.categoryMediumName ?? '',
  } as Card;
}

function normalizeSeller(raw: any): Seller {
  return {
    id: raw?.id ?? 0,
    nickname: raw?.nickname ?? '알 수 없음',
    reviewCount: raw?.reviewCount ?? 0,
    // 타입이 number 라면 null 방지를 위해 0으로 보정
    reviewStarAvg: typeof raw?.reviewStarAvg === 'number' ? raw.reviewStarAvg : 0,
    profileImageUrl: raw?.profileImageUrl ?? '/no-image.jpg',
  } as Seller;
}

function normalizeHistory(list: any[]): History[] {
  return (list ?? []).map((h) => ({
    createdAt: h?.createdAt ?? h?.time ?? '',
    bidPrice: h?.bidPrice ?? h?.price ?? 0,
    bidderNickname: h?.bidderNickname ?? h?.nickname ?? '',
  })) as History[];
}

function normalizeWeekly(list: any[]): WeeklyAuctions[] {
  return (list ?? []).map((p) => ({
    date: p?.date ?? '',
    minPrice: p?.minPrice ?? 0,
    maxPrice: p?.maxPrice ?? 0,
    avgPrice: p?.avgPrice ?? 0,
  })) as WeeklyAuctions[];
}

function normalizeAuctionDetail(result: RawResponse['result']): AuctionDetailProps {
  const a = result.auction ?? {};
  const c = result.card ?? {};

  const card = normalizeCard(c);
  // auction.grade 가 진짜 카드 등급이면 카드에 반영
  if (a?.grade && typeof a.grade === 'string') {
    card.grade = a.grade;
  }

  return {
    id: a?.id ?? 0,
    code: a?.code ?? '',
    title: a?.title ?? '',
    detail: a?.detail ?? '',

    imageUrls: result?.imageUrls ?? [],

    startPrice: a?.startPrice ?? 0,
    currentPrice: a?.currentPrice ?? 0,
    bidUnit: a?.bidUnit ?? 1,

    endTime: a?.endTime ?? a?.endsAt ?? '',
    createAt: a?.createAt ?? a?.createdAt ?? '',
    end: Boolean(a?.end ?? a?.isEnded ?? false),

    buyNowFlag: Boolean(a?.buyNowFlag ?? a?.buyNow ?? false),
    buyNowPrice: a?.buyNowPrice ?? null,

    card,
    weeklyAuctions: normalizeWeekly(result?.weeklyPrices),
    history: normalizeHistory(result?.history),
    seller: normalizeSeller(result?.seller),
  } as AuctionDetailProps;
}

export type AuctionDetailPageData = {
  detail: AuctionDetailProps;
  wished: boolean;
};

export async function getAuctionDetail(
  auctionId: number | string,
  opts?: { historySize?: number; signal?: AbortSignal }
): Promise<AuctionDetailPageData> {
  const { historySize = 5, signal } = opts || {};
  const res = await api.get<RawResponse>(`/v1/auctions/${auctionId}`, { params: { historySize }, signal });

  const payload = res.data;
  if (!payload?.isSuccess) throw new Error(payload?.message || '요청 실패');

  return {
    detail: normalizeAuctionDetail(payload.result),
    wished: Boolean(payload.result?.wished),
  };
}