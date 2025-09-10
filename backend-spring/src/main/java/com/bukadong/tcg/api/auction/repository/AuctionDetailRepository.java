package com.bukadong.tcg.api.auction.repository;

import com.bukadong.tcg.api.auction.dto.response.AuctionDetailResponse.*;
import com.bukadong.tcg.api.auction.entity.Auction;

import java.util.List;

/**
 * 경매 상세 전용 커스텀 리포지토리 (QueryDSL/Native 혼용)
 * <P>
 * 상세 페이지 구성을 위한 맵핑/집계/히스토리 기능을 제공한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN DTO 매핑 결과
 */
public interface AuctionDetailRepository {

    /**
     * 엔티티 → AuctionInfo 매핑
     * <P>
     * 필드/플래그 포맷팅 등 비즈니스 포맷 변환을 담당.
     * </P>
     * 
     * @PARAM auction 경매 엔티티
     * @RETURN AuctionInfo
     */
    AuctionInfo mapAuctionInfo(Auction auction);

    /**
     * 엔티티 → CardInfo 매핑
     * 
     * @PARAM auction 경매 엔티티(연관된 카드/카테고리 사용)
     * @RETURN CardInfo
     */
    CardInfo mapCardInfo(Auction auction);

    /**
     * 경매 이미지 URL 목록 조회
     * 
     * @PARAM auctionId 경매 ID
     * @RETURN 이미지 URL 목록
     */
    List<String> findImageUrlsByAuctionId(Long auctionId);

    /**
     * 최근 7일 일자별(min/max/avg) 시세 라인
     * <P>
     * 모든 경매 중 동일 카드(card_id) 기준, auction_result의 최종 입찰가를 이용.
     * </P>
     * 
     * @PARAM cardId 카드 ID
     * @RETURN 일자별 라인
     */
    List<DailyPriceLine> findWeeklyPriceLinesByCardId(Long cardId);

    /**
     * 입찰 히스토리 조회
     * 
     * @PARAM auctionId 경매 ID
     * @PARAM limit 최대 개수
     * @RETURN 최근순(내림차순) 리스트
     */
    List<BidHistoryItem> findBidHistory(Long auctionId, int limit);
}
