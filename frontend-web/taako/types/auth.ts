export interface AuthState {
    token: string | null;
    loading: boolean;
    error: string | null;
    login: (email: string, password: string) => Promise<boolean>;
    refreshAccessToken: () => Promise<string | null>;
    logout: () => void;
  }

  // 내 프로필 조회
export interface MyInfo {
  backgroundImageUrl: string|null;
  email: string|null;
  introduction: string|null;
  mumberId: number;
  nickname: number;
  notificationsSetting: Object|null;
  profileImageUrl: string|null;
}

// 내 입찰 경매 목록 조회
export interface MyBidAuctions {
  auctionId: number;
  code: string|null;
  title: string|null;
  startDatetime: string|null;
  endDatetime: string|null;
  isEnd: boolean|null;
  closeReason: string|null;
  currentPrice: number|null;
  myTopBidAmount: number|null;
  imageUrl: string|null;
  bids: [
    {
      time: string|null;
      nickname: string|null;
      price: number|null;
    }
  ]
}