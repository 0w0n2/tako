'use client';

import SearchAuctionFilter from '@/components/filters/SearchAuctionFilter';
import AuctionCard from '@/components/cards/AuctionCard'
import { useSearchParams } from 'next/navigation';
import Pagenation from '@/components/atoms/Pagenation';

export default function SearchClient() {    const searchParams = useSearchParams();
  const query = searchParams.get('cardName') || '';

  return (
    <div>
            <div className="default-container pb-[60px]">
                <div className='flex items-center gap-2 mb-4'>
                    <h2>{query ? `'${query}'` : ''} 검색결과</h2>
                    <span className='text-sm text-[#a5a5a5]'>총 2개</span>
                </div>

                {/* 검색 필터 컴포넌트 */}
                <SearchAuctionFilter />
                {/*  */}

                {/* 경매 */}
                <div className='mt-15'>
                    <ul className='grid grid-cols-5 gap-8'>
                        <li>
                            <AuctionCard />
                        </li>
                        <li>
                            <AuctionCard />
                        </li>
                    </ul>
                </div>

                {/* 페이지네이션 */}
                <div className='mt-8'>
                    <Pagenation />
                </div>
            </div>
        </div>

  )
}