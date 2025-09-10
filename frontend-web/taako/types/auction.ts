import { Card } from "./card";
import { Seller } from "./seller";

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
  imageUrl: string[];
  price: number;
  endTime: string;
  create_at: string;
  categoryMediumName: string;
  card: Card;
  seller: Seller;
}