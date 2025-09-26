// components/review/AddReviews.tsx
'use client'

import { useEffect } from 'react';
import { useReview } from '@/hooks/useReview';
import { useMyInfo } from '@/hooks/useMyInfo';
import AddReviewsAuctionComponents from './AddReviewsAuctionComponents'

interface AddReviewsProps {
  memberId: number;
}

export default function AddReviews({ memberId }: AddReviewsProps) {
  const { handleGetReview, reviews } = useReview();
  const { endedAuctions } = useMyInfo();

  const confirmedAuctions = endedAuctions.filter(
    (item) => item.delivery?.status === 'CONFIRMED'
  );

  useEffect(() => {
    handleGetReview(memberId);
  }, [memberId]);

  if (confirmedAuctions.length === 0) {
    return <p className="text-center text-gray-400 py-20">구매 확정된 경매가 없습니다.</p>;
  }

  return (
    <div>
      {confirmedAuctions.map((item, index) => (
        <AddReviewsAuctionComponents key={index} item={item} />
      ))}
    </div>
  );
}
