import { Card } from "./card";
import { Seller } from "./seller";
import { History } from "./history";

// 관심 경매
export interface AuctionCardProps {
  id: number;
  grade: string;
  title: string;
  bigCount: number;
  remainingSeconds: number;
  primaryImageUrl: string;
}

// 상세 페이지
export interface AuctionDetailProps {
  id: number;
  code: string;
  title: string;
  detail: string;
  imageUrls: string[];
  startPrice: number;
  currentPrice: number;
  bidUnit: number;
  endTime: string;
  createAt: string;
  end: boolean;
  buyNowFlag: boolean;
  buyNowPrice: number | null;
  card: Card;
  weeklyAuctions: WeeklyAuctions[],
  history: History[];
  seller: Seller;
}

// 경매 목록 조회(request)
export interface GetHotCards {
  page: number | null;
  categoryMajorId: number | null;
  categoryMediumId: number | null;
  title: string | null;
  cardId: number | null;
  currentPriceMin: number | null;
  currentPriceMax: number | null;
  grades: string | null;
  sort: string | null;
}
// 경매 목록 조회(response)
export interface GetAuction {
  id: number;
  grade: string;
  title: string;
  currentPrice: number;
  bidCount: number;
  remainingSeconds: number;
  primaryImageUrl: string;
  wished: boolean
}


export interface WeeklyAuctions {
  date: string;
  minPrice: number;
  maxPrice: number;
  avgPrice: number;
}

// 경매 등록 폼
export interface AuctionFormProps {
  files: File[];
  requestDto: {
    gradeHash: string | null,
    categoryMajorId: number | null,
    categoryMediumId: number | null,
    cardId: number | null,
    title: string;
    detail: string;
    startDatetime: string;
    endDatetime: string;
    buyNowFlag: boolean;
    buyNowPrice: number;
    bidUnit: number;
    startPrice: number;
  }
}

// 내 경매 목록 응답
export interface MyAuctionResponse {
  auctionId: number;
  title: string;
  imageUrl: string | null;
  isEnd: boolean;
  closeReason: string | null;
  currentPrice: number;
  bids: {
    nickname: string;
    price: number;
  }[];
  startDatetime: string;
  endDatetime: string;
}