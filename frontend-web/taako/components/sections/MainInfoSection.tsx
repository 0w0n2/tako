'use client'
// Import Swiper React components
import { Navigation, Pagination } from 'swiper/modules';
import { Swiper, SwiperSlide } from 'swiper/react';
import Image from 'next/image';
import Link from 'next/link';
// Import Swiper styles
import 'swiper/css';

export default function MainInfoSection() {
    return (
        <div className="relative overflow-hidden" style={{
            backgroundImage: "url(/background/main-bg.png)",
            backgroundSize: "cover",
            backgroundPosition: "center",
            backgroundRepeat: "no-repeat"
        }}>
            {/* 배경 이미지 오브젝트 */}
            {/* <div className='absolute top-[50%] left-[10%] opacity-20'>
                <Image src="/background/bg-object1.png" alt="object1" width={500} height={300}/>
            </div> */}
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
                            <Link href="#pokemon">
                                <div>
                                    <h2 className='main'>Pokemon</h2>
                                    <p>포켓몬</p>
                                </div>
                                <div className='img'>
                                    <Image className='object-fit w-full h-full' src="/main-card/pokemon.png" alt="pokomon" width={100} height={300} />
                                ~</div>
                            </Link>
                        </SwiperSlide>
                        <SwiperSlide className='bg-gradient-to-b from-pink-800 to-black/50 backdrop-blur-xl'>
                            <Link href="#yugioh">
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
                            <Link href="cookierun">
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