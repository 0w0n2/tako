import api from "./api";

// 리뷰 조회
export async function getMyReview(memberId: number) {
  const res = await api.get(`/v1/reviews/${memberId}/reviews`);
  return res.data;
}

// 리뷰 작성
export async function addMyReview(auctionId:number, cardCondition:string, priceSatisfaction:string, descriptionMatch:string, star:number, reviewText:string|null){
    const res = await api.post(`/v1/reviews`, {
        auctionId, cardCondition, priceSatisfaction, descriptionMatch, star, reviewText,
    });
    return res.data;
}