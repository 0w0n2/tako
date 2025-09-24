package com.bukadong.tcg.api.auction.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bukadong.tcg.api.auction.dto.projection.AuctionListProjection;

import java.math.BigDecimal;
import java.util.Set;

/**
 * 경매 목록 QueryDSL 검색 리포지토리
 * <P>
 * 동적 필터/정렬/페이지네이션을 제공한다.
 * </P>
 */
public interface AuctionRepositoryCustom {

    /**
     * 경매 목록 검색
     * <P>
     * 필터는 모두 선택적이며 AND 조합으로 적용된다.
     * </P>
     *
     * @param categoryMajorId  카테고리 대분류 ID(옵션)
     * @param categoryMediumId 카테고리 중분류 ID(옵션)
     * @param titlePart        타이틀 부분일치(옵션)
     * @param cardId           카드 ID(옵션)
     * @param currentPriceMin  현재가 최소(옵션)
     * @param currentPriceMax  현재가 최대(옵션)
     * @param grades           등급 집합(옵션)
     * @param sort             정렬 기준(옵션, null이면 id DESC)
     * @param includeEnded     종료된 경매 포함 여부 (true면 종료/진행 모두 포함, false면 진행중만)
     * @param pageable         페이지 정보(서비스에서 size=20 강제)
     * @return 내부 행 DTO 페이지
     */
    @SuppressWarnings("java:S107")
    Page<AuctionListProjection> searchAuctions(Long categoryMajorId, Long categoryMediumId, String titlePart,
            Long cardId, BigDecimal currentPriceMin, BigDecimal currentPriceMax, Set<String> grades, AuctionSort sort,
            boolean includeEnded, Pageable pageable);

    boolean isDuplicatedTokenId(Long tokenId);
}