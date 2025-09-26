'use client'

import Link from 'next/link';
import { useState, useEffect } from 'react';

import api from "@/lib/api"
import EffectCard from "@/components/cards/EffectCard"
import CardInfo from "@/components/cards/CardInfo"
import Loading from '@/components/Loading';
import AuctionCard from "@/components/auction/AuctionCard";
import Pagination from "@/components/atoms/PaginationComponent";
import { useAuctionsQuery } from "@/hooks/useAuctionsQuery";
import { GetAuction } from "@/types/auction";
import attributeMap from './attribute_map.json';

export default function CategoryItemPage({ params }: { params: { categoryId: string, CardId: string } }) {
  const [isLoading, setIsLoading] = useState(true);
  const [cardData, setCardData] = useState<any>(null);
  const [description, setDescription] = useState<any>(null);
  const [currentPage, setCurrentPage] = useState(0);

  const cardTypes = {
    1 : "YuGiOh", 
    2 : "Pokémon", 
    3 : "Cookierun", 
  }

  const cardType = cardTypes[Number(params.categoryId) as 1 | 2 | 3]

  // 속성 매핑 함수
  const mapAttribute = (originalAttribute: string, cardType: string): string => {
    const typeKey = cardType.toLowerCase() as keyof typeof attributeMap;
    const mapping = attributeMap[typeKey] as Record<string, string>;
    
    if (mapping && mapping[originalAttribute]) {
      return mapping[originalAttribute];
    }
    
    // 매핑되지 않은 경우 기본값 반환
    return 'fire';
  }

  // 카드 타입별 속성 추출 함수
  const extractAttribute = (description: any, cardType: string): string => {
    switch (cardType) {
      case 'pokemon':
        // Pokémon: types 배열에서 첫 번째 요소 사용
        if (description.types && Array.isArray(description.types) && description.types.length > 0) {
          return mapAttribute(description.types[0].toLowerCase(), cardType);
        }
        break;
      case 'yugioh':
        // YuGiOh: attribute 문자열 사용
        if (description.attribute) {
          return mapAttribute(description.attribute, cardType);
        }
        break;
      case 'cookierun':
        // CookieRun: energyType 문자열 사용
        if (description.energyType) {
          return mapAttribute(description.energyType, cardType);
        }
        break;
    }
    
    // 기본값
    return 'fire';
  }

  // 스네이크 케이스를 소문자+공백 형태로 변환하는 함수
  const formatRarity = (snakeCaseRarity: string): string => {
    return snakeCaseRarity
      .toLowerCase()
      .split('_')
      .join(' ');
  }

  // 카드별 진행중인 경매 조회
  const { data: auctionsData, isLoading: auctionsLoading, isError: auctionsError } = useAuctionsQuery({
    cardId: Number(params.CardId),
    page: currentPage
  });

  const auctions: GetAuction[] = (auctionsData?.result?.content ?? []) as GetAuction[];
  const totalPages: number = (auctionsData?.result?.totalPages ?? 1) as number;

  useEffect(() => {
    const fetchCardData = async () => {
      try {
        const response = await api.get(`/v1/cards/${params.CardId}`)
        const data = response.data.result
        const desc = JSON.parse(data.description)
        
        // 카드 타입별로 속성 추출
        const mappedAttribute = extractAttribute(desc, cardType);
        desc.mappedAttribute = mappedAttribute;
        
        setCardData(data);
        setDescription(desc);
      } catch (error) {
        console.error('Error fetching card data:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchCardData();
  }, [params.CardId, params.categoryId, cardType]);

  if (isLoading) {
    return (
      <div>
        <Loading />
      </div>
    );
  }

  if (!cardData || !description) {
    return (
      <div className="default-container pb-[80px] relative">
        <div className="text-center py-20">
          <p className="text-[#a5a5a5]">카드 정보를 불러올 수 없습니다.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="default-container pb-[80px] relative">
      <div>
        <div className='pb-5 border-b border-[#353535]'>
          <div className='flex gap-1 text-[#a5a5a5] mb-3'>
            <Link href={`/search?categoryMajorId=${params.categoryId}`}>{cardType}</Link>
            {`>`}
            <Link href={`/category/${params.categoryId}`}>{cardData.name}</Link>
          </div>
          {/* 제목 */}
          <h2>{cardData.name}</h2>
        </div>

        <div className='flex py-[60px]'>
          {/* 이미지 */}
          <div className='w-[40%] px-[40px] flex justify-center flex-1 border-r border-[#353535] self-start'>
            <EffectCard
              type={cardType as 'pokemon' | 'yugioh' | 'cookierun' | 'SSAFY'}
              attribute={description.mappedAttribute as 'fire' | 'water' | 'grass' | 'lightning' | 'psychic' | 'fighting' | 'darkness' | 'metal' | 'dragon' | 'fairy'}
              rarity={formatRarity(cardData.rarity) as any}
              img={cardData.imageUrls[0]}
            />
          </div>

          {/* 내용 */}
          <div className='w-[60%] px-[40px]'>
            <CardInfo 
              cardData={cardData}
              description={description}
              cardType={cardType}
            />
          </div>
        </div>
      </div>

      {/* 진행중인 경매 섹션 */}
      <div className="mt-16">
        <div className="pb-5 border-b border-[#353535] mb-8">
          <h2 className="text-2xl font-bold">진행중인 경매</h2>
          <p className="text-sm text-[#a5a5a5] mt-2">총 {auctions.length}개</p>
        </div>

        <div>
          {auctionsLoading && (
            <p className="flex justify-center items-center text-[#a5a5a5] h-40">경매 목록을 불러오는 중...</p>
          )}
          {auctionsError && (
            <p className="text-red-500 text-center py-8">경매 목록을 불러오는 중 오류가 발생했습니다.</p>
          )}
          {!auctionsLoading && auctions.length === 0 && (
            <p className="flex justify-center items-center text-[#a5a5a5] h-40">
              현재 진행중인 경매가 없습니다.
            </p>
          )}
          {!auctionsLoading && auctions.length > 0 && (
            <>
              <ul className="grid grid-cols-5 gap-8">
                {auctions.map((auction) => (
                  <li key={auction.id}>
                    <AuctionCard item={auction} />
                  </li>
                ))}
              </ul>
              
              {totalPages > 1 && (
                <div className="mt-8">
                  <Pagination 
                    currentPage={currentPage} 
                    totalPages={totalPages} 
                    onPageChange={setCurrentPage} 
                  />
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}