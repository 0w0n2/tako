'use client'

import Image from 'next/image';
import RankElement from '@/components/atoms/RankElement';
import BidInputForm from '@/components/atoms/BidInputForm';

import AuctionDetailImages from '@/components/sections/AuctionDetailImages';
import { AuctionDetailProps } from '@/types/auction';

// 더미 데이터
const dummy_auction: AuctionDetailProps[] = [
  {
    id: 1,
    imageUrl: [
      '/auction/106bb00f55200ab3b5770ae9508a4172.jpg',
      '/auction/123.jpg',
    ],
    price: 1.052,
    endTime: '2025.10.10 10:00:00',
    create_at: '2025.09.10 16:20:50',
    categoryMediumName: '피카츄',
    card: {
      id: 1,
      name: '레귤레이션A',
      grade: 'S+',
    },
    seller: {
      id: 1,
      name: '판매자A',
      reviewCount: 12,
      reviewAverage: 4.2,
    },
  },
];

export default function AuctionDetailPage() {

  return (
    <div className="default-container pb-[80px]">
      <div>
        <div className='flex'>
          {/* Product Images */}
          <div className='flex-1 flex justify-center border-r border-[#353535]'>
            <AuctionDetailImages props={dummy_auction[0]} />
          </div>

          {/* 내용 */}
          <div className='flex-1 px-[50px]'>
            <p>현재 입찰가</p>
            <p className="font-bold text-[32px]">{dummy_auction[0].price} TKC</p>

            <div className='mt-10'>
              <p className='text-[20px]'>남은 시간</p>

            </div>
            {/* 옵션 */}
            <div>
              <ul className='grid grid-cols-4 place-items-center mt-10'>
                <li><RankElement rank={dummy_auction[0].card.grade} /></li>
                <li>
                  <p className='text-sm text-[#aaaaaa] mb-1'>등록일</p>
                  <p>2025년 9월 10일</p>
                </li>
                <li>
                  <p className='text-sm text-[#aaaaaa] mb-1'>중분류</p>
                  <p>{dummy_auction[0].categoryMediumName}</p>
                </li>
                <li>
                  <p className='text-sm text-[#aaaaaa] mb-1'>카드명</p>
                  <p>{dummy_auction[0].card.name}</p>
                </li>
              </ul>
            </div>

            {/* 버튼 */}
            <div className='flex gap-4 place-items-center mt-10'>
              <button className='rounded-md flex-1 py-5 bg-[#838383] text-[#D5D5D5]'>즉시구매 불가</button>
              <button className='rounded-md border-1 border-[#353535] flex-1 py-5 flex gap-2 justify-center items-center'>
                <Image src="/icon/heart-white.svg" alt="wish-detail" width={15} height={15}/>
                <p>관심상품</p>
              </button>
            </div>

            {/* 입찰 */}
            <div className='mt-10'>
              <p className='text-[20px]'>입찰 하기</p>
              <BidInputForm />
            </div>
          </div>
        </div>
        <div>
          <h2>상세내용</h2>
        </div>
      </div>
    </div>
  );
}
