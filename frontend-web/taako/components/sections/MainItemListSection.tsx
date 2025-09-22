'use client'

import AuctionCard from "@/components/auction/AuctionCard"
import { useEffect, useState } from "react";
import { useAuction } from "@/hooks/useAuction";

import { Swiper, SwiperSlide } from 'swiper/react';
import 'swiper/css';
import 'swiper/css/navigation';
import { Navigation } from 'swiper/modules';

export default function MainItemListSection({id}:{id:number}) {
    const { handlerGetAuctions, loading, error } = useAuction();
    const [auctions, setAuctions] = useState([]);

    useEffect(() => {
        const fetch = async () => {
            try {
                const res = await handlerGetAuctions({ categoryMajorId:id }); // 포켓몬
                setAuctions(res.result.content);
            } catch (err) {
                console.error("경매 데이터 로딩 실패:", err);
            }
        };
        fetch();
    }, [handlerGetAuctions]);

    return (
        <div className="default-container">
            {loading ? (
                <div className="flex justify-center items-center h-50 text-sm text-[#a5a5a5]">
                    경매를 불러오는 중입니다
                </div>
            ) : error ? (
                <div className="flex justify-center items-center h-50 text-sm text-red-500">
                    경매 데이터를 불러오는데 실패했습니다
                </div>
            ) : auctions.length === 0 ? (
                <div className="flex justify-center items-center h-50 text-sm text-[#a5a5a5]">
                    등록된 경매가 없습니다
                </div>
            ) : (
                <div>
                    <Swiper
                        slidesPerView={5}
                        spaceBetween={30}
                        navigation={true}
                        modules={[Navigation]}
                        className={`category-${id}`}>
                        {auctions.map((item, index) => (
                            <SwiperSlide key={index}>
                                <AuctionCard item={item} />
                            </SwiperSlide>
                        ))}
                    </Swiper>
                </div>
            )}
        </div>
    )
}