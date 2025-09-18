import api from "@/lib/api";

export const getInfo = async () => {
    const res = await api.get("/v1/members/me");
    return res.data;
};