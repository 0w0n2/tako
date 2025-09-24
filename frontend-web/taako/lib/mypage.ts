import api from "@/lib/api";
import type { MyBidAuctions } from "@/types/auth";

type Page<T> = { content: T[]; page: number; size: number; totalElements: number; totalPages: number };

type BidFilter =
  | { ended: boolean; page?: number; size?: number }     // ✔ /v1/mypage/bids?ended=true|false
  | { isEnd: boolean; page?: number; size?: number }     // ✔ ...?isEnd=true|false
  | { status: "ONGOING" | "ENDED"; page?: number; size?: number } // ✔ ...?status=...
  | { page?: number; size?: number };                    // 기본

export async function getMyBidAuction(opts: BidFilter = {}): Promise<Page<MyBidAuctions>> {
  const { page = 0, size = 10 } = opts as any;
  const params: Record<string, any> = { page, size };

  // 우선순위: ended -> isEnd -> status
  if ('ended' in opts) params.ended = opts.ended;
  else if ('isEnd' in opts) params.isEnd = opts.isEnd;
  else if ('status' in opts) params.status = opts.status;

  const res = await api.get("/v1/auctions/mybid", { params });
  return res.data.result as Page<MyBidAuctions>;
}

export async function getInfo() {
  const res = await api.get("/v1/auctions/me");
  return res.data.result;
}
