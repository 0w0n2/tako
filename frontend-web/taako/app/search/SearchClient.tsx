'use client';

import { useEffect, useState } from 'react';
import { useSearchParams } from 'next/navigation';

import SearchAuctionFilter from '@/components/filters/SearchAuctionFilter';
import AuctionCard from '@/components/auction/AuctionCard';
import Pagination from '@/components/atoms/Pagination';
import { useAuction } from '@/hooks/useAuction';
import { GetAuction } from '@/types/auction';

export default function SearchClient() {
  const searchParams = useSearchParams();

  // URL 쿼리 키-값을 모두 가져와 객체로 변환
  const params: Record<string, string> = {};
  searchParams.forEach((value, key) => {
    params[key] = value;
  });

  const { handlerGetAuctions, loading, error } = useAuction();
  const [auctions, setAuctions] = useState<GetAuction[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  useEffect(() => {
    if (Object.keys(params).length === 0) return;

    const fetchAuctions = async () => {
      const res = await handlerGetAuctions({
        ...params,
        page: currentPage, // 페이지 정보도 같이 전달
      });
      
      // res가 { result: GetAuction[], totalPages: number } 형태라 가정
      const items = res.result || res; 
      setAuctions(items);
      setTotalPages(res.totalPages || 1);
    };

    fetchAuctions();
  }, [searchParams.toString(), currentPage, handlerGetAuctions]); 
  // searchParams 객체 변경 시 effect 재실행

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
          {loading && <p>불러오는 중...</p>}
          {error && <p className="text-red-500">{error}</p>}
          {!loading && auctions.length === 0 && <p>검색 결과가 없습니다.</p>}

          {!loading && auctions.length > 0 && (
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
