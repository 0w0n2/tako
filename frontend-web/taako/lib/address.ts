import api from "./api";
import { AddressRequest } from "@/types/address";

// 배송지 등록
export const addMyAddress = async(placeName:string, name:string, phone:string, baseAddress:string, addressDetail:string, zipcode:string, setAsDefault:boolean) => {
    const res = await api.post("/v1/addresses", {
        placeName,
        name,
        phone,
        baseAddress,
        addressDetail,
        zipcode,
        setAsDefault,
    });
    return res.data;
}

// 배송지 조회
export const getMyAddress = async() => {
    const res = await api.get("/v1/addresses");
    return res.data;
}

// 배송지 삭제
export const deleteMyAddress = async(addressId:number) => {
    const res = await api.delete(`/v1/addresses/${addressId}`);
    return res.data;
}

// 기본 배송지 등록
export const defaultAddAddress = async(addressId:number) => {
    const res = await api.post(`/v1/addresses/${addressId}/default`);
    return res.data;
}

// 기본 배송지 조회
export const defaultGetAddress = async() => {
    const res = await api.post(`/v1/addresses/default`);
    return res.data;
}
