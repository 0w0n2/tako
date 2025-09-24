// 등록용
export interface AddressRequest {
    placeName: string|null;
    name: string|null;
    phone: string|null;
    baseAddress: string|null;
    addressDetail: string|null;
    zipcode: string|null;
    setAsDefault: boolean;
}
// 조회용
export interface AddressResponse {
    id: number;
    placeName: string;
    baseAddress: string;
    zipcode: string;
    default: boolean;
}


// 기본 배송지 조회
export interface GetDefaultAddress {
    id: number;
    placeName: string;
    baseAddress: string;
    zipcode: string;
}