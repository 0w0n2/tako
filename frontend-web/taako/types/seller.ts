import { AuctionCardProps } from "./auction";

export interface Seller {
    id: number;
    nickname: string;
    reviewCount: number;
    reviewStarAvg: number;
    profileImageUrl: string;
    introduction: string;
    trustScore: number;
    sellAuctions: AuctionCardProps[];
}