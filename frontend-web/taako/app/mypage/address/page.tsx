'use client'

import { useState } from "react";
import { Button } from "@/components/ui/button";
import AddAddress from "@/components/mypage/AddAddress";
import { useAddress } from "@/hooks/useAddress";

export default function MyAddressPage() {
    const { address, defaultAddress, handlerDeleteAddress, handlerDefalutAddress } = useAddress();
    const [isModalOpen, setIsModalOpen] = useState(false);

    const handleOpenModal = () => setIsModalOpen(true);
    const handleCloseModal = () => setIsModalOpen(false);

    return (
        <div>
            <div className="flex justify-between items-end mb-8">
                <div>
                    <h2>주소록</h2>
                </div>
                <Button variant="outline" className="cursor-pointer" onClick={handleOpenModal}>
                    + 새 주소 추가하기
                </Button>
            </div>

            {address ? (
                <div className="flex flex-col gap-5">
                    <div>
                        <h3 className="py-4">기본 배송지</h3>
                        <div>
                            {defaultAddress.map((item, index) => (
                                <div key={index} className="bg-gray-800 p-5 rounded-xl flex justify-between items-center">
                                    <div className="flex flex-col gap-1">
                                        <p className="font-semibold">{item.placeName}</p>
                                        <p className="text-sm">({item.zipcode}){item.baseAddress}</p>
                                    </div>
                                    <div className="flex gap-2">
                                        <Button className="py-3 px-7" variant="outline">수정</Button>
                                        <Button onClick={() => handlerDeleteAddress(item.id)} className="py-3 px-7" variant="outline">삭제</Button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                    <div>
                        <h3 className="py-4">저장된 주소</h3>
                        <div className="flex flex-col">
                            {address.map((item, index) => (
                                <div key={index} className="py-8 flex justify-between items-center border-b border-[#353535]">
                                    <div className="flex flex-col gap-1">
                                        <p className="font-semibold">{item.placeName}</p>
                                        <p className="text-sm">({item.zipcode}){item.baseAddress}</p>
                                    </div>
                                    <div className="flex gap-2">
                                        <Button onClick={() => handlerDefalutAddress(item.id)} className="py-3 px-7" variant="outline">기본 배송지</Button>
                                        <Button className="py-3 px-7" variant="outline">수정</Button>
                                        <Button onClick={() => handlerDeleteAddress(item.id)} className="py-3 px-7" variant="outline">삭제</Button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            ) : (
                <div className="py-40 text-sm text-[#a5a5a5] text-center">
                    배송지 정보가 없습니다.<br />
                    새 주소를 등록해주세요
                </div>
            )}

            {/* 모달 */}
            {isModalOpen && <AddAddress onClose={handleCloseModal} />}
        </div>
    );
}
