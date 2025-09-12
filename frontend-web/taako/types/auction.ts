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

export interface WeeklyAuctions {
  date: string;
  minPrice: number;
  maxPrice: number;
  avgPrice: number;
}