"use client";

import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "next/navigation";
import SearchAuctionFilter from "@/components/filters/SearchAuctionFilter";
import AuctionCard from "@/components/auction/AuctionCard";
import Pagination from "@/components/atoms/Pagination";
import { useAuctionsQuery } from "@/hooks/useAuctionsQuery";
import { GetAuction } from "@/types/auction";

export default function SearchClient() {
  const searchParams = useSearchParams();
  const [currentPage, setCurrentPage] = useState(0);

  // URL 쿼리에서 값 읽기
  const params = useMemo(() => {
    return {
      title: searchParams.get("title") || "",
      categoryMajorId: Number(searchParams.get("categoryMajorId")) || undefined,
      categoryMediumId: Number(searchParams.get("categoryMediumId")) || undefined,
      currentPriceMin: Number(searchParams.get("currentPriceMin")) || undefined,
      currentPriceMax: Number(searchParams.get("currentPriceMax")) || undefined,
    };
  }, [searchParams]);

  // 쿼리가 바뀔 때 페이지 초기화
  useEffect(() => {
    setCurrentPage(0);
  }, [params.categoryMajorId, params.categoryMediumId, params.title, params.currentPriceMin ,params.currentPriceMax]);

  const { data, isLoading, isError } = useAuctionsQuery({ ...params, page: currentPage });

  const auctions: GetAuction[] = (data?.result?.content ?? []) as GetAuction[];
  const totalPages: number = (data?.result?.totalPages ?? 1) as number;

  // console.log("검색 파라미터:", params);
  // console.log("검색 결과:", auctions);

  return (
    <div>
      <div className="default-container pb-[60px]">
        <div className="flex items-center gap-2 mb-4">
          <h2>{params.title ? `${params.title}` : ""} 검색결과</h2>
          <span className="text-sm text-[#a5a5a5]">총 {auctions.length}개</span>
        </div>

        <SearchAuctionFilter />

        <div className="mt-15">
          {isLoading && <p className="flex justify-center items-center text-[#a5a5a5] h-40">불러오는 중...</p>}
          {isError && <p className="text-red-500">검색 중 오류가 발생했습니다.</p>}
          {!isLoading && auctions.length === 0 && (
            <p className="flex justify-center items-center text-[#a5a5a5] h-40">
              검색 결과가 없습니다.
            </p>
          )}
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

        <div className="mt-8">
          <Pagination currentPage={currentPage} totalPages={totalPages} onPageChange={setCurrentPage} />
        </div>
      </div>
    </div>
  );
}
