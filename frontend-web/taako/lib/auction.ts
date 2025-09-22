import api from "./api";
import { GetHotCards } from "@/types/auction";

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