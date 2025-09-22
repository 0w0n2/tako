'use client';

import { useMemo, useState } from 'react';
import { useSearchParams } from 'next/navigation';

import SearchAuctionFilter from '@/components/filters/SearchAuctionFilter';
import AuctionCard from '@/components/auction/AuctionCard';
import Pagination from '@/components/atoms/Pagination';
import { useAuctionsQuery } from '@/hooks/useAuctionsQuery';
import { GetAuction } from '@/types/auction';

export default function SearchClient() {
  const searchParams = useSearchParams();

  // URL 쿼리 키-값을 모두 가져와 객체로 변환 (메모이즈)
  const params = useMemo(() => {
    const obj: Record<string, string> = {};
    searchParams.forEach((value, key) => {
      obj[key] = value;
    });
    return obj;
  }, [searchParams]);

  const [currentPage, setCurrentPage] = useState(1);
  const { data, isLoading, isError } = useAuctionsQuery(
    { ...params, page: currentPage } as any,
    { enabled: Object.keys(params).length > 0 }
  );

  const auctions: GetAuction[] = (data?.result?.content ?? []) as GetAuction[];
  const totalPages: number = (data?.result?.totalPages ?? 1) as number;

  return (
    <div>
      <div className="default-container pb-[60px]">
        {/* 검색 결과 타이틀 */}
        <div className="flex items-center gap-2 mb-4">
          <h2>{params.title ? `'${params.title}'` : ''} 검색결과</h2>
          <span className="text-sm text-[#a5a5a5]">
            총 {auctions.length}개
          </span>
        </div>

        {/* 검색 필터 */}
        <SearchAuctionFilter />

        {/* 경매 목록 */}
        <div className="mt-15">
          {isLoading && <p>불러오는 중...</p>}
          {isError && <p className="text-red-500">검색 중 오류가 발생했습니다.</p>}
          {!isLoading && auctions.length === 0 && <p>검색 결과가 없습니다.</p>}

          {!isLoading && auctions.length > 0 && (
            <ul className="grid grid-cols-5 gap-8">
              {auctions.map((item) => (
                <li key={item.id}>
                  <AuctionCard item={item} />
                </li>
              ))}
            </ul>
          )}
        </div>

        {/* 페이지네이션 */}
        <div className="mt-8">
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={(page) => setCurrentPage(page)}
          />
        </div>
      </div>
    </div>
  );
}
