"use client";
import { useState, useEffect } from "react";

import api from "@/lib/api";
import EffectCard from "@/components/cards/EffectCard";
import CardInfo from "@/components/cards/CardInfo";
import CardGradeDailyStatsChart from "@/components/charts/parts/CardGradeDailyStatsChart";
import Loading from "@/components/Loading";
import AuctionCard from "@/components/auction/AuctionCard";
import CategoryPagination from "@/components/categories/categoryPagination";
import { useAuctionsQuery } from "@/hooks/useAuctionsQuery";
import { useCardWish } from "@/hooks/useCardWish";
import { GetAuction } from "@/types/auction";
import attributeMap from "./attribute_map.json";

export default function CategoryItemPage({ params }: Readonly<{ params: Readonly<{ categoryId: string; CardId: string }> }>) {
	const [isLoading, setIsLoading] = useState(true);
	const [cardData, setCardData] = useState<any>(null);
	const [description, setDescription] = useState<any>(null);
	const [currentPage, setCurrentPage] = useState(0);

	const cardTypes = {
		1: "YuGiOh",
		2: "Pokémon",
		3: "Cookierun",
		4: "SSAFY",
	};

	const cardType = cardTypes[Number(params.categoryId) as 1 | 2 | 3 | 4];

	// 속성 매핑 함수
	const mapAttribute = (originalAttribute: string, cardType: string): string => {
		const typeKey = cardType.toLowerCase() as keyof typeof attributeMap;
		const mapping = attributeMap[typeKey] as Record<string, string>;

		if (mapping?.[originalAttribute]) {
			return mapping[originalAttribute];
		}

		// 매핑되지 않은 경우 기본값 반환
		return "fire";
	};

	// 카드 타입별 속성 추출 함수
	const extractAttribute = (description: any, cardType: string): string => {
		const type = cardType.toLowerCase();
		switch (type) {
			case "pokemon":
				// Pokémon: types 배열에서 첫 번째 요소 사용
				if (description.types && Array.isArray(description.types) && description.types.length > 0) {
					return mapAttribute(description.types[0].toLowerCase(), cardType);
				}
				break;
			case "yugioh":
				// YuGiOh: attribute 문자열 사용
				if (description.attribute) {
					return mapAttribute(description.attribute, cardType);
				}
				break;
			case "cookierun":
				// CookieRun: energyType 문자열 사용
				if (description.energyType) {
					return mapAttribute(description.energyType, cardType);
				}
				break;
		}

		// 기본값
		return "fire";
	};

	// 스네이크 케이스를 소문자+공백 형태로 변환하는 함수
	const formatRarity = (snakeCaseRarity: string): string => {
		return snakeCaseRarity.toLowerCase().split("_").join(" ");
	};

	// 카드별 진행중인 경매 조회
	const {
		data: auctionsData,
		isLoading: auctionsLoading,
		isError: auctionsError,
	} = useAuctionsQuery({
		cardId: Number(params.CardId),
		page: currentPage,
	});

	const auctions: GetAuction[] = (auctionsData?.result?.content ?? []) as GetAuction[];
	const totalPages: number = (auctionsData?.result?.totalPages ?? 1) as number;

	// 카드 wish 기능
	const { wished, pendingWish, wishError, toggleWish } = useCardWish(Number(params.CardId), cardData?.wished || false);

	useEffect(() => {
		const fetchCardData = async () => {
			try {
				const response = await api.get(`/v1/cards/${params.CardId}`);
				const data = response.data.result;
				const desc = JSON.parse(data.description);

				// 카드 타입별로 속성 추출
				const mappedAttribute = extractAttribute(desc, cardType);
				desc.mappedAttribute = mappedAttribute;

				setCardData(data);
				setDescription(desc);
			} catch (error) {
				console.error("Error fetching card data:", error);
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
				<div className="flex py-[60px]">
					{/* 이미지 */}
					<div className="w-[40%] px-[40px] flex flex-col items-center flex-1 border-r border-[#353535] self-start">
						<EffectCard
							type={cardType as "pokemon" | "yugioh" | "cookierun" | "ssafy"}
							attribute={description.mappedAttribute as "fire" | "water" | "grass" | "lightning" | "psychic" | "fighting" | "darkness" | "metal" | "dragon" | "fairy"}
							rarity={formatRarity(cardData.rarity) as any}
							img={cardData.imageUrls[0]}
						/>

						{/* 관심 카드 버튼 */}
						<div className="mt-8 w-full max-w-[300px]">
							<button
								onClick={toggleWish}
								disabled={pendingWish}
								aria-pressed={wished}
								className={`rounded-md border-1 border-[#353535] w-full py-4 flex gap-2 justify-center items-center transition
        ${wished ? "bg-[#2a2a2a] border-[#ff5a5a]" : "hover:bg-white/5"}`}
								title={wished ? "관심카드에서 제거" : "관심카드에 추가"}
							>
								<svg
									width="18"
									height="18"
									viewBox="0 0 24 24"
									fill={wished ? "#ff5a5a" : "none"}
									stroke={wished ? "#ff5a5a" : "#ffffff"}
									strokeWidth="2"
									strokeLinecap="round"
									strokeLinejoin="round"
									aria-hidden="true"
								>
									<path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 1 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
								</svg>
								<p>
									{(() => {
										if (wished) return pendingWish ? "해제 중..." : "관심카드";
										return pendingWish ? "추가 중..." : "관심카드";
									})()}
								</p>
							</button>
							{wishError && !wishError.canceled && <p className="mt-2 text-red-400 text-sm text-center">{wishError.safeMessage || "관심카드 처리 중 오류가 발생했어요."}</p>}
						</div>
					</div>

					{/* 내용 */}
					<div className="w-[60%] px-[40px]">
						<CardInfo cardData={cardData} description={description} cardType={cardType} />

						{/* 카드 시세 그래프 */}
						<div className="mt-14">
							<p className="text-[20px]">최근 7일 카드 등급별 낙찰가 추이</p>
							<p className="text-sm text-[#a5a5a5] mt-2">카드 등급별 일별 최대/평균/최소 낙찰가를 표시합니다. 데이터가 없는 날도 그래프가 끊기지 않고 이어집니다.</p>
							<div className="mt-6">
								<CardGradeDailyStatsChart cardId={Number(params.CardId)} days={7} />
							</div>
						</div>
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
					{auctionsLoading && <p className="flex justify-center items-center text-[#a5a5a5] h-40">경매 목록을 불러오는 중...</p>}
					{auctionsError && <p className="text-red-500 text-center py-8">경매 목록을 불러오는 중 오류가 발생했습니다.</p>}
					{!auctionsLoading && auctions.length === 0 && <p className="flex justify-center items-center text-[#a5a5a5] h-40">현재 진행중인 경매가 없습니다.</p>}
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
									<CategoryPagination currentPage={currentPage} totalPages={totalPages} onPageChange={setCurrentPage} />
								</div>
							)}
						</>
					)}
				</div>
			</div>
		</div>
	);
}
