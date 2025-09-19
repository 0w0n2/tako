import api from "./api";

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