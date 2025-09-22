package com.bukadong.tcg.api.auction.service;

import com.bukadong.tcg.api.auction.converter.AuctionListConverter;
import com.bukadong.tcg.api.auction.dto.response.AuctionDetailResponse;
import com.bukadong.tcg.api.auction.dto.response.AuctionListItemResponse;
import com.bukadong.tcg.api.auction.repository.AuctionDetailRepository;
import com.bukadong.tcg.api.auction.repository.AuctionRepository;
import com.bukadong.tcg.api.auction.repository.AuctionRepositoryCustom;
import com.bukadong.tcg.api.auction.repository.AuctionSort;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.service.MediaUrlService;
import com.bukadong.tcg.api.wish.repository.auction.WishAuctionRepository;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.dto.PageResponse;
import com.bukadong.tcg.global.common.exception.BaseException;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import static com.bukadong.tcg.api.auction.entity.QAuction.auction;

/**
 * 경매 목록 조회 서비스
 * <P>
 * 파라미터 검증/페이지 강제/남은시간 계산을 담당한다.
 * </P>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionQueryService {

    private static final ZoneOffset UTC = ZoneOffset.UTC;

    private final AuctionRepositoryCustom auctionRepositoryCustom;
    private final AuctionDetailRepository auctionDetailRepository;
    private final AuctionRepository auctionRepository; // 상세 조회용 fetch-graph 사용
    private final MediaUrlService mediaUrlService;
    private final WishAuctionRepository wishAuctionRepository;
    private final JPAQueryFactory queryFactory;

    /**
     * 경매 목록 조회 서비스(컨트롤러 편의 오버로드)
     * <P>
     * 컨트롤러가 page(int)만 넘겨도 되도록 내부에서 Pageable(size=20)을 구성하고, PageResponse로 변환해
     * 반환합니다. 동일 클래스 내 트랜잭션 메서드 호출을 피하여 Sonar 규칙(java:S6809)도 만족합니다.
     * </P>
     * 
     * @PARAM categoryMajorId 카테고리 대분류 ID
     * @PARAM categoryMediumId 카테고리 중분류 ID
     * @PARAM titlePart 타이틀 부분검색어
     * @PARAM cardId 카드 ID
     * @PARAM currentPriceMin 현재가 최소
     * @PARAM currentPriceMax 현재가 최대
     * @PARAM grades 등급 집합
     * @PARAM sort 정렬 기준 (null이면 ENDTIME_ASC)
     * @PARAM page 0-base 페이지 인덱스
     * @PARAM memberId 로그인 회원 ID(없으면 null)
     * @RETURN PageResponse<AuctionListItemResponse>
     */
    public PageResponse<AuctionListItemResponse> getAuctionList(Long categoryMajorId, Long categoryMediumId,
            String titlePart, Long cardId, BigDecimal currentPriceMin, BigDecimal currentPriceMax, Set<String> grades,
            AuctionSort sort, int page, Long memberId) {
        Pageable pageable = PageRequest.of(page, 20);

        var rows = auctionRepositoryCustom.searchAuctions(categoryMajorId, categoryMediumId, titlePart, cardId,
                currentPriceMin, currentPriceMax, grades, (sort == null ? AuctionSort.ENDTIME_ASC : sort), pageable);

        // 현재 페이지의 경매 ID들
        List<Long> ids = rows.getContent().stream().map(r -> r.id()).toList();

        // 로그인 회원의 위시된 경매 ID 집합(없으면 비어있는 셋)
        Set<Long> wishedIds = (memberId == null || ids.isEmpty()) ? Set.of()
                : Set.copyOf(wishAuctionRepository.findWishedAuctionIds(memberId, ids));

        Duration ttl = Duration.ofMinutes(30);
        List<AuctionListItemResponse> items = rows.getContent().stream()
                .map(row -> AuctionListConverter.toItem(row, mediaUrlService, ttl, wishedIds.contains(row.id())))
                .toList();

        Page<AuctionListItemResponse> p = new PageImpl<>(items, pageable, rows.getTotalElements());
        return PageResponse.from(p);
    }

    /**
     * 경매 상세 조회
     * <P>
     * 히스토리는 기본 5개이며, 요청 파라미터로 개수 조절 가능.
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @PARAM historySize 히스토리 개수
     * @RETURN AuctionDetailResponse
     */
    public AuctionDetailResponse getDetail(Long auctionId, int historySize, Long memberId) {
        var auction = auctionRepository.findByIdWithCardAndCategory(auctionId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));

        var auctionInfo = auctionDetailRepository.mapAuctionInfo(auction);
        var cardInfo = auctionDetailRepository.mapCardInfo(auction);

        List<String> imageUrls = mediaUrlService.getPresignedImageUrls(MediaType.AUCTION_ITEM, auctionId,
                Duration.ofMinutes(5));
        var weeklyPrices = auctionDetailRepository.findWeeklyPriceLinesByCardId(auction.getCard().getId());
        var history = auctionDetailRepository.findBidHistory(auctionId, historySize);
        var sellerInfo = auctionDetailRepository.findSellerInfoByAuctionId(auctionId);
        boolean wished = false;
        if (memberId != null) {
            wished = wishAuctionRepository.existsByMemberIdAndAuctionIdAndWishFlagTrue(memberId, auctionId);
        }
        return AuctionDetailResponse.builder().auction(auctionInfo).card(cardInfo).weeklyPrices(weeklyPrices)
                .history(history).imageUrls(imageUrls).seller(sellerInfo).wished(wished).build();
    }

    /**
     * 마감 도달 + 미종료 경매 ID 조회
     * <P>
     * 엔티티 필드명(isEnd, endDatetime)에 맞춰 QueryDSL 경로 수정.
     * </P>
     * 
     * @PARAM limit 최대 반환 개수
     * @RETURN 경매 ID 리스트
     */
    public List<Long> findDueAuctionIds(int limit) {
        LocalDateTime now = LocalDateTime.now(UTC);
        return queryFactory.select(auction.id).from(auction)
                .where(auction.isEnd.isFalse(), auction.endDatetime.loe(now))
                .orderBy(auction.endDatetime.asc(), auction.id.asc()).limit(limit).fetch();
    }

}
