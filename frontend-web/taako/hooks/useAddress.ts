'use client'

import { addMyAddress, getMyAddress, deleteMyAddress, defaultAddAddress } from "@/lib/address";
import { AddressRequest, AddressResponse } from "@/types/address";
import { useEffect, useState } from "react";

export function useAddress() {
  const [address, setAddress] = useState<AddressResponse[]>([]);
  const defaultAddress = address.filter(item => item.default == true)

  // 배송지 조회
  const handlerGetAddress = async () => {
    try {
      const res = await getMyAddress();
      setAddress(res.result);
    } catch (err) {
      console.error("주소 불러오기 실패:", err);
    }
  };

  useEffect(() => {
    handlerGetAddress();
  }, []);

   // 배송지 추가
  const handlerAddAddress = async (placeName:string, name:string, phone:string, baseAddress:string, addressDetail:string, zipcode:string, setAsDefault:boolean) => {
    try {
      await addMyAddress(placeName, name, phone, baseAddress, addressDetail, zipcode, setAsDefault);
      await handlerGetAddress();
    } catch (err) {
      console.error("주소 추가 실패:", err);
    }
  };

  // 배송지 삭제
  const handlerDeleteAddress = async (addressId:number) => {
    try{
        await deleteMyAddress(addressId);
        setAddress(prev => prev.filter(item => item.id !== addressId));
    }catch(err){
        console.error(err);
    }
  }

  // 기본 배송지 등록
  const handlerDefalutAddress = async (addressId:number) => {
    try{
        await defaultAddAddress(addressId);
        alert("기본 배송지가 변경되었습니다.")
        await handlerGetAddress();
    }catch(err){
        console.error(err);
    }
  }

  return {
    handlerAddAddress, handlerDeleteAddress, handlerDefalutAddress,
    address, defaultAddress,
  };
};
