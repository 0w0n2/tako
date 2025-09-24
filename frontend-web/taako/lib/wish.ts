// lib/wish.ts
import api from "./api";

export async function addWishAuction(auctionId: number | string, signal?: AbortSignal) {
  const res = await api.post(`/v1/wishes/auctions/${auctionId}`, null, { signal });
  return res.data; // { isSuccess, ... } 형태라면 호출부에서 체크해도 됨
}

export async function removeWishAuction(auctionId: number | string, signal?: AbortSignal) {
  const res = await api.delete(`/v1/wishes/auctions/${auctionId}`, { data: {}, signal });
  return res.data;
}
