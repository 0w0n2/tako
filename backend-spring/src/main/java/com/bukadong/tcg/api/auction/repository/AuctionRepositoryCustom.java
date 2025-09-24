package com.bukadong.tcg.api.auction.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bukadong.tcg.api.auction.dto.projection.AuctionListProjection;

import java.math.BigDecimal;
import java.util.Set;

/**
 * 경매 목록 QueryDSL 검색 리포지토리
 * <p>
 * 동적 필터/정렬/페이지네이션을 제공한다.
 * </P>
 */
public interface AuctionRepositoryCustom {

    /**
     * 경매 목록 검색
     * <p>
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
     * @param sort             정렬 기준(필수)
     * @param pageable         페이지 정보(서비스에서 size=20 강제)
     * @return 내부 행 DTO 페이지
     */
    Page<AuctionListProjection> searchAuctions(Long categoryMajorId, Long categoryMediumId, String titlePart,
                                               Long cardId, BigDecimal currentPriceMin, BigDecimal currentPriceMax, Set<String> grades, AuctionSort sort,
                                               Pageable pageable);

    boolean isDuplicatedTokenId(Long tokenId);

    /**
     * 회원 탈퇴 가능 여부를 확인
     * 아래 조건 중 하나라도 해당되면 탈퇴할 수 없으므로 true를 반환
     * 1. 판매자로서 종료되지 않은 경매가 있는 경우
     * 2. 구매자로서 입찰한 경매 중 종료되지 않은 경매가 있는 경우
     * 3. 판매자 또는 낙찰자로서 거래가 완료(구매 확정)되지 않은 경매가 있는 경우
     *
     * @param memberId 확인할 회원의 ID
     * @return 탈퇴 불가능한 경매 관련 이력이 있으면 true, 없으면 false
     */
    boolean existsActiveAuctionAsSeller(Long memberId);

    boolean existsBidOnActiveAuction(Long memberId);

    boolean existsUnsettledAuctionAsParty(Long memberId);

}