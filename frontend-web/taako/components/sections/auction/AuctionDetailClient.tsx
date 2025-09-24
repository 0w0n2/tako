'use client';

import { useState } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import Loading from '@/components/Loading';
import RankElement from '@/components/atoms/RankElement';
import BidInputForm from '@/components/atoms/BidInputForm'; // ← 경로 수정
import AuctionDetailImages from '@/components/sections/auction/AuctionDetailImages';
import RemainingTime from '@/components/atoms/RemainingTime';
import AuctionChart from '@/components/charts/AuctionChart';
import AuctionInquiry from '@/components/sections/auction/AuctionInquiry';
import { useAuctionDetail } from '@/hooks/useAuctionDetail';
import { useAuctionPrice } from '@/hooks/useAuctionPrice';

type Props = {
  auctionId: number;
  historySize?: number;
};

export default function AuctionDetailClient({ auctionId, historySize = 5 }: Props) {
  const { data, loading, error, wished, pendingWish, wishError, toggleWish } =
    useAuctionDetail(auctionId, historySize);

  // 문의 개수
  const [inqTotal, setInqTotal] = useState<number>(0);
  const handleTotalChange = (n: number) => {
    setInqTotal((prev) => (prev === n ? prev : n)); // 같은 값이면 렌더 스킵
  };

  // 상세 로드된 현재가를 초기값으로, SSE+폴링으로 계속 갱신
  const initial = typeof data?.currentPrice === 'number' ? data.currentPrice : 0;
  const [currentPrice, setCurrentPrice] = useAuctionPrice(auctionId, initial, {
    sseUrl: `${process.env.NEXT_PUBLIC_API_BASE_URL}/v1/auctions/${auctionId}/live`,
    pollUrl: `/v1/auctions/${auctionId}`,
    withCredentials: true, // 세션 쿠키 사용 시
    pollMs: 4000,
    getPrice: (j) => j?.result?.currentPrice ?? j?.currentPrice,
  });

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

  const auc = data;
  if (!auc.card) {
    return (
      <div className="default-container pb-[80px]">
        <p>카드 정보가 없어요. 잠시 후 다시 시도해주세요.</p>
      </div>
    );
  }

  const displayPrice = (currentPrice ?? auc.currentPrice) as number;
  const minStep = 0.01 as number;

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
              {/* 통화 표기는 Sepolia ETH */}
              <p className="-mt-1 text-[40px]">{displayPrice} ETH</p>
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
              <button
                onClick={toggleWish}
                disabled={pendingWish}
                aria-pressed={wished}
                className={`rounded-md border-1 border-[#353535] flex-1 py-4 flex gap-2 justify-center items-center transition
                  ${wished ? 'bg-[#2a2a2a] border-[#ff5a5a]' : 'hover:bg-white/5'}`}
                title={wished ? '관심경매에서 제거' : '관심경매에 추가'}
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill={wished ? '#ff5a5a' : 'none'} stroke={wished ? '#ff5a5a' : '#ffffff'} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 1 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                </svg>
                <p>{wished ? (pendingWish ? '추가 중...' : '관심경매') : (pendingWish ? '해제 중...' : '관심경매')}</p>
              </button>
            </div>

            {wishError && !wishError.canceled && (
              <p className="mt-2 text-red-400 text-sm">
                {wishError.safeMessage || '관심상품 처리 중 오류가 발생했어요.'}
              </p>
            )}

            {/* 입찰 */}
            <div className="mt-4">
              <BidInputForm
                auctionId={auctionId}
                currentPrice={displayPrice}            // ← 로컬/서버 값 사용
                minIncrement={auc.bidUnit || 0.01}                    // ← 숫자 타입으로 전달
                onBidApplied={(nextPrice) => setCurrentPrice(nextPrice)} // 성공 시 즉시 반영
              />
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
                      unoptimized
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
          <h2 className="text-[20px]">경매문의 ({inqTotal})</h2>
          <div className="mt-3">
            <AuctionInquiry props={auc} onTotalChange={handleTotalChange} />
          </div>
        </div>
      </div>
    </div>
  );
}
