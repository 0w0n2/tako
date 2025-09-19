'use client'

import { Navigation, Pagination } from 'swiper/modules';
import { Swiper, SwiperSlide } from 'swiper/react';
import Image from 'next/image';
import Link from 'next/link';
import { useState, useEffect } from 'react';
import api from '@/lib/api';
import { MajorCategories } from '@/types/category';
import 'swiper/css';

export default function MainInfoSection() {
    const [categoryIdTable, setCategoryIdTable] = useState<MajorCategories[]>([])

    useEffect(() => {
        const fetchCategories = async () => {
            try {
                const response = await api.get("v1/categories/majors")
                setCategoryIdTable(response.data.result)
            } catch (error) {
                console.error("카테고리 정보를 불러오는데 실패했습니다.", error)
            }
        }
        fetchCategories()
    }, [])

    const getCategoryId = (categoryName: string): number => {
        const found = categoryIdTable.find((item: MajorCategories) => item.name === categoryName)
        return found ? found.id : 0
    }
    return (
        <div className="relative overflow-hidden" style={{
            backgroundImage: "url(/background/main-bg.png)",
            backgroundSize: "cover",
            backgroundPosition: "center",
            backgroundRepeat: "no-repeat"
        }}>

            <div className="default-container flex flex-col">
                <div className='flex flex-col gap-5 mt-[160px] items-center text-center'>
                    <h1 className='main'>Next-Gen<br/>
                    TCG Auctions</h1>
                    <p className='text-lg text-[#a5a5a5]'>희귀한 TCG 카드를 더욱 가치 있게.<br/> 안전한 블록체인 기반 경매 시스템으로 당신의 거래를 투명하게 보장합니다.</p>
                </div>
                <div>
                    <Swiper
                        modules={[Navigation, Pagination]}
                        spaceBetween={-80}
                        slidesPerView={5}
                        centeredSlides={true}
                        className="mySwiper translate-y-40"
                        onSwiper={(swiper) => console.log(swiper)}
                        onSlideChange={() => console.log('slide change')}
                    >
                        <SwiperSlide className='bg-gradient-to-b from-yellow-300 to-black/30 backdrop-blur-xl'>
                            <Link href={`/category/${getCategoryId('Pokémon')}?categoryName=Pokémon`}>
                                <div>
                                    <h2 className='main'>Pokemon</h2>
                                    <p>포켓몬</p>
                                </div>
                                <div className='img'>
                                    <Image className='object-fit w-full h-full' src="/main-card/pokemon.png" alt="pokomon" width={100} height={300} />
                                </div>
                            </Link>
                        </SwiperSlide>
                        <SwiperSlide className='bg-gradient-to-b from-pink-800 to-black/50 backdrop-blur-xl'>
                            <Link href={`/category/${getCategoryId('Yu-Gi-Oh!')}?categoryName=Yu-Gi-Oh!`}>
                                <div>
                                    <h2 className='main'>Yu-Gi-OH!</h2>
                                    <p>유희왕</p>
                                </div>
                                <div className='img'>
                                    <Image className='object-fit w-full h-full' src="/main-card/yugioh.png" alt="pokomon" width={100} height={300} />
                                </div>
                            </Link>
                        </SwiperSlide>
                        <SwiperSlide className='bg-gradient-to-b from-orange-800 to-black/30 backdrop-blur-xl'>
                            <Link href={`/category/${getCategoryId('Cookierun')}?categoryName=Cookierun`}>
                                <div>
                                    <h2 className='main'>CookieRun</h2>
                                    <p>쿠키런</p>
                                </div>
                                <div className='absolute top-34 left-1/2 -translate-x-1/2 w-full object-fit'>
                                    <Image className='object-fit w-full h-full' src="/main-card/cookierun.webp" alt="pokomon" width={100} height={300} />
                                </div>
                            </Link>
                        </SwiperSlide>
                    </Swiper>
                </div>
            </div>
            {/* 하단 그라디언트 페이드 오버레이 */}
            {/* <div className="pointer-events-none z-10 absolute inset-x-0 bottom-0 h-15 bg-gradient-to-t from-[#141420] via-[#141420]/60 to-transparent" /> */}
        </div>
    )
}