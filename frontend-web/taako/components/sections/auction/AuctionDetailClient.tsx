// components/sections/auction/AuctionDetailClient.tsx
'use client';

import Image from 'next/image';
import Link from 'next/link';
import Loading from '@/components/Loading';
import RankElement from '@/components/atoms/RankElement';
import BidInputForm from '@/components/atoms/BidInputForm';
import AuctionDetailImages from '@/components/sections/auction/AuctionDetailImages';
import RemainingTime from '@/components/atoms/RemainingTime';
import AuctionChart from '@/components/charts/AuctionChart';
import AuctionInquiry from '@/components/sections/auction/AuctionInquiry';
import { useAuctionDetail } from '@/hooks/useAuctionDetail';

type Props = {
  auctionId: number;
  historySize?: number;
};

export default function AuctionDetailClient({ auctionId, historySize = 5 }: Props) {
  const { data, loading, error } = useAuctionDetail(auctionId, historySize);

  if (loading) {
    return (
      <div className="default-container pb-[80px]">
        <Loading />
      </div>
    );
  }

  if (error) {
    return (
      <div className="default-container pb-[80px]">
        <p className="text-red-400">경매 정보를 불러오는 중 오류가 발생했어요.</p>
      </div>
    );
  }

  if (!data) {
    return (
      <div className="default-container pb-[80px]">
        <p>경매 정보를 찾을 수 없어요.</p>
      </div>
    );
  }
  
  console.log(data)
  // ---- data는 AuctionDetailProps 타입이라고 가정 ----
  const auc = data;

  if (!auc.card) {
    return (
        <div className="default-container pb-[80px]">
        <p>카드 정보가 없어요. 잠시 후 다시 시도해주세요.</p>
        </div>
    );
    }

  return (
    <div className="default-container pb-[80px] relative">
      <div>
        <div className="pb-5 border-b border-[#353535]">
          <div className="flex gap-1 text-[#a5a5a5] mb-3">
            <Link href={`/search?categoryMajorId=${auc.card?.categoryMajorId ?? ''}`}>
                {auc.card?.categoryMajorName ?? '카테고리'}
            </Link>
            {` > `}
            <Link
                href={`/search?categoryMajorId=${auc.card?.categoryMajorId ?? ''}&categoryMediumId=${auc.card?.categoryMediumId ?? ''}`}
            >
                {auc.card?.categoryMediumName ?? '중분류'}
            </Link>
            {` > `}
            <Link
                href={`/search?categoryMajorId=${auc.card?.categoryMajorId ?? ''}&categoryMediumId=${auc.card?.categoryMediumId ?? ''}&cardId=${auc.card?.id ?? ''}`}
            >
                {auc.card?.name ?? '카드'}
            </Link>
          </div>
          <h2>{auc.title}</h2>
        </div>

        <div className="flex py-[60px]">
          {/* 이미지 */}
          <div className="w-[50%] px-[40px] flex justify-center flex-1 border-r border-[#353535] sticky top-[160px] z-10 self-start">
            <AuctionDetailImages props={auc} />
          </div>

          {/* 내용 */}
          <div className="w-[50%] px-[40px]">
            <div>
              <p className="text-[#ddd]">현재 입찰가</p>
              <p className="-mt-1 text-[40px]">{auc.currentPrice} TKC</p>
            </div>

            {/* 경매 속성 */}
            <ul className="mt-6 mb-10 flex flex-col gap-4">
              <li className="flex items-end">
                <p className="w-[90px] text-[#aaaaaa]">컨디션</p>
                <RankElement rank={auc.card.grade} />
              </li>
              <li className="flex items-end">
                <p className="w-[90px] text-[#aaaaaa]">등록일</p>
                <p>{auc.createAt}</p>
              </li>
              <li className="flex items-end">
                <p className="w-[90px] text-[#aaaaaa]">남은 시간</p>
                <RemainingTime props={auc} />
              </li>
            </ul>

            {/* 버튼 */}
            <div className="flex gap-4 place-items-center">
              {auc.buyNowFlag ? (
                <button className="rounded-md flex-1 py-4 bg-[#7db7cd] text-[#000] hover:bg-[#5a9bb8] transition-colors duration-200 cursor-pointer">
                  즉시구매
                </button>
              ) : (
                <button className="rounded-md flex-1 py-4 bg-[#838383] text-[#D5D5D5] cursor-not-allowed" disabled>
                  즉시구매 불가
                </button>
              )}
              <button className="rounded-md border-1 border-[#353535] flex-1 py-4 flex gap-2 justify-center items-center cursor-pointer">
                <Image src="/icon/heart-white.svg" alt="wish-detail" width={15} height={15} />
                <p>관심상품</p>
              </button>
            </div>

            {/* 입찰 */}
            <div className="mt-4">
              <BidInputForm props={auc} />
            </div>

            {/* 히스토리 */}
            <div className="mt-10">
              <p className="text-[20px]">히스토리</p>
              <p className="text-sm text-[#a5a5a5] mt-2">
                지난 7일간 해당 카드와 같은 등급의 거래 내역입니다.
              </p>
              <div className="mt-10">
                <AuctionChart props={auc} />
              </div>
            </div>

            {/* 판매자 정보 */}
            <div className="mt-10">
              <Link href={`/shop/${auc.seller.id}`}>
                <p className="text-[20px]">판매자 정보</p>
                <div className="mt-4 flex gap-4 items-center">
                  <div className="rounded-full overflow-hidden w-20 h-20">
                    <Image
                      className="w-full h-full object-fit"
                      src={`${auc.seller.profileImageUrl || '/no-image.jpg'}`}
                      width={80}
                      height={80}
                      alt="profile-image"
                    />
                  </div>
                  <div>
                    <p>{auc.seller.nickname}</p>
                    <div className="flex gap-1 items-center mt-0.5">
                      <div>
                        <Image src="/icon/star.png" alt="review" width={20} height={20} />
                      </div>
                      <p className="text-[#aaa]">
                        {auc.seller.reviewStarAvg} ({auc.seller.reviewCount})
                      </p>
                    </div>
                  </div>
                </div>
              </Link>
            </div>
          </div>
        </div>

        {/* 상세내용 */}
        <div className="py-15 border-t border-b border-[#353535]">
          <h2 className="text-[20px]">상세내용</h2>
          <p className="mt-7">{auc.detail}</p>
        </div>

        {/* 배송 */}
        <div className="py-15 border-b border-[#353535]">
          <h2 className="text-[20px]">배송</h2>
          <p className="mt-7">{auc.detail}</p>
        </div>

        {/* 문의 */}
        <div className="pt-15">
          <h2 className="text-[20px]">경매문의 (0)</h2>
          <div className="mt-3">
            <AuctionInquiry props={auc} />
          </div>
        </div>
      </div>
    </div>
  );
}
