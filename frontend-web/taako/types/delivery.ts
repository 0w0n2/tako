// 경매 배송 정보 조회
export interface GetAuctionDelivery {
    createdAt: string|null;
    updatedAt: string|null;
    id: number|null;
    senderAddress: {
      id: number|null;
      placeName: string|null;
      name: string|null;
      phone: string|null;
      baseAddress: string|null;
      addressDetail: string|null;
      zipcode: string|null;
    },
    recipientAddress: {
      id: number|null;
      placeName: string|null;
      name: string|null;
      phone: string|null;
      baseAddress: string|null;
      addressDetail: string|null;
      zipcode: string|null;
    },
    trackingNumber: string|null;
    status: string|null;
}