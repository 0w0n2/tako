import api from "@/lib/api";

// 내 프로필 조회
export const getInfo = async () => {
    const res = await api.get("/v1/members/me");
    return res.data;
};

// 내 입찰 경매 조회
export const getMyBidAuction = async() => {
    const res = await api.get("/v1/auctions/mybid");
    return res.data;
}

// 내 판매 경매 조회
export const getMySellAutcion = async () => {
    const res = await api.get("/v1/auctions/me");
    return res.data;
};