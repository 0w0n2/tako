'use client'

import Image from 'next/image';
import Link from 'next/link';
import { useState, useEffect } from 'react';

import RankElement from '@/components/atoms/RankElement';
import BidInputForm from '@/components/atoms/BidInputForm';
import AuctionDetailImages from '@/components/sections/auction/AuctionDetailImages';
import RemainingTime from '@/components/atoms/RemainingTime';
import AuctionChart from '@/components/charts/AuctionChart';
import Loading from '@/components/Loading';

import { AuctionDetailProps } from '@/types/auction';
import AuctionInquiry from '@/components/sections/auction/AuctionInquiry';

// 더미 데이터
const dummy_auction: AuctionDetailProps[] = [
  {
    id: 1,
    code: "AUC030012",
    title: "포켓몬 트레이너 튤립 팝니다. 열심히 입찰해주세요",
    detail: "더미 경매 (RAND) - day_back 1",
    imageUrls: [
      '/auction/106bb00f55200ab3b5770ae9508a4172.jpg',
      '/auction/123.jpg',
    ],
    startPrice: 79.05,
    currentPrice: 320.052,
    bidUnit: 0.001,
    buyNowFlag: true,
    buyNowPrice: null,
    endTime: '2025-09-15T03:41:06.261636',
    createAt: '2025-09-10T19:41:06.261636',
    end: false,
    weeklyAuctions: [
      {
        "date": "2025-09-05",
        "minPrice": 168.28724297,
        "maxPrice": 543.50321929,
        "avgPrice": 318.510927082143
      },
      {
        "date": "2025-09-06",
        "minPrice": 118.29562168,
        "maxPrice": 645.70742113,
        "avgPrice": 337.685500076429
      },
      {
        "date": "2025-09-07",
        "minPrice": 73.11424872,
        "maxPrice": 351.95289614,
        "avgPrice": 221.644853701429
      },
      {
        "date": "2025-09-08",
        "minPrice": 34.04467983,
        "maxPrice": 270.05298576,
        "avgPrice": 120.781875277143
      },
      {
        "date": "2025-09-09",
        "minPrice": 157.9692468,
        "maxPrice": 987.14065963,
        "avgPrice": 356.029214697857
      },
      {
        "date": "2025-09-10",
        "minPrice": 100.17886704,
        "maxPrice": 720.59845549,
        "avgPrice": 279.255470942857
      }
    ],
    card: {
      id: 1,
      name: '피카츄',
      grade: 'S+',
      rarity: 'DEFAULT',
      categoryMajorId: 1005,
      categoryMajorName: "포켓몬",
      categoryMediumId: 10042,
      categoryMediumName: "레귤레이션A",
    },
    seller: {
      id: 1,
      nickname: '판매자A',
      reviewCount: 12,
      reviewStarAvg: 4.2,
      profileImageUrl: '/no-image.jpg'
    },
    history:[
      {
        "createdAt": "2025-09-10T18:06:50.511919",
        "bidPrice": 320.09881015,
        "bidderNickname": "u5163"
      },
      {
        "createdAt": "2025-09-10T06:37:19.37837",
        "bidPrice": 301.23237875,
        "bidderNickname": "u5131"
      },
      {
        "createdAt": "2025-09-10T00:17:34.085732",
        "bidPrice": 288.73237875,
        "bidderNickname": "u5021"
      },
      {
        "createdAt": "2025-09-09T19:25:21.798634",
        "bidPrice": 287.73237875,
        "bidderNickname": "u5190"
      },
      {
        "createdAt": "2025-09-09T18:18:35.924934",
        "bidPrice": 267.73237875,
        "bidderNickname": "u5191"
      }
    ],
  },
];

export default function AuctionDetailPage() {
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // 컴포넌트가 마운트된 후 로딩 상태 해제
    setIsLoading(false);
  }, []);

  if (isLoading) {
    return (
      <div>
        <Loading />
      </div>
    );
  }

  return (
    <div className="default-container pb-[80px] relative">
      <div>
        <div className='pb-5 border-b border-[#353535]'>
          <div className='flex gap-1 text-[#a5a5a5] mb-3'>
            <Link href={`/search?categoryMajorId=${dummy_auction[0].card.categoryMajorId}`}>{dummy_auction[0].card.categoryMajorName}</Link>
            {`>`}
            <Link href={`/search?categoryMajorId=${dummy_auction[0].card.categoryMajorId}?categoryMediunId=${dummy_auction[0].card.categoryMediumId}`}>{dummy_auction[0].card.categoryMediumName}</Link>
            {`>`}
            <Link href={`/search?categoryMajorId=${dummy_auction[0].card.categoryMajorId}?categoryMediunId=${dummy_auction[0].card.categoryMediumId}?cardId=${dummy_auction[0].card.id}`}>{dummy_auction[0].card.name}</Link>
          </div>
          {/* 제목 */}
          <h2>{dummy_auction[0].title}</h2>
        </div>

        <div className='flex py-[60px]'>
          {/* 이미지 */}
          <div className='w-[50%] px-[40px] flex justify-center flex-1 border-r border-[#353535] sticky top-[160px] z-10 self-start'>
            <AuctionDetailImages props={dummy_auction[0]} />
          </div>

          {/* 내용 */}
          <div className='w-[50%] px-[40px]'>
            <div className=''>
              <p className='text-[#ddd]'>현재 입찰가</p>
              <p className="-mt-1 text-[40px]">{dummy_auction[0].currentPrice} TKC</p>
            </div>
            
            {/* 경매 속성 */}
            <ul className="mt-6 mb-10 flex flex-col gap-4">
              <li className="flex items-end">
                <p className='w-[90px] text-[#aaaaaa]'>컨디션</p>
                <RankElement rank={dummy_auction[0].card.grade} />
              </li>
              <li className="flex items-end">
                <p className="w-[90px] text-[#aaaaaa]">등록일</p>
                <p>{dummy_auction[0].createAt}</p>
              </li>
              <li className="flex items-end">
                <p className='w-[90px] text-[#aaaaaa]'>남은 시간</p>
                <RemainingTime props={dummy_auction[0]} />
              </li>
            </ul>

            {/* 버튼 */}
            <div className='flex gap-4 place-items-center'>
              {dummy_auction[0].buyNowFlag ? (
                <button className='rounded-md flex-1 py-4 bg-[#7db7cd] text-[#000] hover:bg-[#5a9bb8] transition-colors duration-200 cursor-pointer'>
                  즉시구매
                </button>
              ) : (
                <button className='rounded-md flex-1 py-4 bg-[#838383] text-[#D5D5D5] cursor-not-allowed' disabled>
                  즉시구매 불가
                </button>
              )}
              <button className='rounded-md border-1 border-[#353535] flex-1 py-4 flex gap-2 justify-center items-center cursor-pointer'>
                <Image src="/icon/heart-white.svg" alt="wish-detail" width={15} height={15}/>
                <p>관심상품</p>
              </button>
            </div>

            {/* 입찰 */}
            <div className=''>
              {/* <p className='text-[20px]'>입찰 하기</p> */}
              <div className='mt-4'>
                <BidInputForm props={dummy_auction[0]} />
              </div>
            </div>

            {/* 히스토리 */}
            <div className='mt-10'>
              <p className='text-[20px]'>히스토리</p>
              <p className='text-sm text-[#a5a5a5] mt-2'>지난 7일간 해당 카드와 같은 등급의 거래 내역입니다.</p>
              <div className='mt-10'>
                <AuctionChart props={dummy_auction[0]}/>
              </div>
            </div>

            {/* 판매자 정보 */}
            <div className='mt-10'>
              <Link href={`/shop/${dummy_auction[0].seller.id}`}>
                <p className='text-[20px]'>판매자 정보</p>
                <div className='mt-4 flex gap-4 items-center'>
                  <div className='rounded-full overflow-hidden w-20 h-20'><Image className='w-full h-full object-fit' src={`${dummy_auction[0].seller.profileImageUrl}`} width={80} height={80} alt="profile-image"/></div>
                  <div>
                    <p>{dummy_auction[0].seller.nickname}</p>
                    <div className='flex gap-1 items-center mt-0.5'>
                      <div><Image src="/icon/star.png" alt="review" width={20} height={20}/></div>
                      <p className='text-[#aaa]'>{dummy_auction[0].seller.reviewStarAvg} ({dummy_auction[0].seller.reviewCount})</p>
                    </div>
                  </div>
                </div>
              </Link>
            </div>
          </div>
        </div>

        {/* 상세내용 */}
        <div className='py-15 border-t border-b border-[#353535]'>
          <h2 className='text-[20px]'>상세내용</h2>
          <p className='mt-7'>{dummy_auction[0].detail}</p>
        </div>

        {/* 배송 */}
        <div className='py-15 border-b border-[#353535]'>
          <h2 className='text-[20px]'>배송</h2>
          <p className='mt-7'>{dummy_auction[0].detail}</p>
        </div>

        {/* 문의 */}
        <div className='pt-15'>
          <h2 className='text-[20px]'>경매문의 (0)</h2>
          <div className='mt-3'>
            <AuctionInquiry props={dummy_auction[0]} />
          </div>
        </div>
      </div>
    </div>
  );
}
