import api from "@/lib/api";

export const getInfo = async () => {
    const res = await api.get("/v1/members/me");
    return res.data;
};

export const getMyBidAuction = async() => {
    const res = await api.get("/v1/auctions/mybid");
    return res.data;
}