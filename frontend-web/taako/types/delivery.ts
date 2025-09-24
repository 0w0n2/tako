export type AddressBrief = {
  id: number;
  placeName: string;
  name: string;
  phone: string;
  baseAddress: string;
  addressDetail: string;
  zipcode: string;
};

export type DeliveryStatus =
  | "WAITING"        // 배송준비중
  | "CONFIRMED"      // 구매확정 완료
  | "IN_PROGRESS"      // 배송중
  | "COMPLETED"     // 배송완료
  | "CANCELLED_BY_USER"      // 미사용
  | string;          

export type DeliveryInfo = {
  createdAt: string;
  updatedAt: string;
  id: number;
  senderAddress?: AddressBrief | null;
  recipientAddress?: AddressBrief | null;
  trackingNumber?: string | null;
  status: DeliveryStatus;
};

export type DeliveryApiResponse<T = DeliveryInfo> = {
  httpStatus: any;
  isSuccess: boolean;
  message: string;
  code: number;
  result: T;
};
