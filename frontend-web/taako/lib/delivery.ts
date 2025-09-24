import api from "./api";

// 경매 배송 정보 조회
export const getAuctionDelivery = async(auctionId:number) => {
    const res = await api.get(`/v1/deliveries/${auctionId}`);
    return res.data;
}

// 판매자: 보내는 주소 설정
export const sellerDelivery = async(auctionId:number, addressId:number) => {
    const res = await api.post(`/v1/deliveries/${auctionId}/sender/${addressId}`);
    return res.data;
}
