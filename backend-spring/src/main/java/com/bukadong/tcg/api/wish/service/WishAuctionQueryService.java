package com.bukadong.tcg.api.wish.service;

import com.bukadong.tcg.api.media.service.MediaUrlService;
import com.bukadong.tcg.api.wish.dto.response.WishAuctionListRow;
import com.bukadong.tcg.api.wish.repository.auction.WishAuctionRepository;
import com.bukadong.tcg.global.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * 위시 경매 조회 서비스
 * <P>
 * 대표 이미지 키를 presigned URL로 변환 후 반환.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishAuctionQueryService {

    private final WishAuctionRepository wishAuctionRepository;
    private final MediaUrlService mediaUrlService;

    /**
     * 내 위시 경매 목록
     * <P>
     * 이미지 필드는 presigned URL로 변환.
     * </P>
     * 
     * @PARAM memberId 회원 ID
     * @PARAM pageable 페이지 정보
     * @RETURN PageResponse<WishAuctionListRow>
     */
    public PageResponse<WishAuctionListRow> list(Long memberId, Pageable pageable) {
        Page<WishAuctionListRow> page = wishAuctionRepository.findMyWishAuctionsRaw(memberId, pageable)
                .map(row -> WishAuctionListRow.builder().auctionId(row.getAuctionId())
                        .imageKey(mediaUrlService.getPresignedUrl(row.getImageKey(), Duration.ofMinutes(5)))
                        .title(row.getTitle()).currentPrice(row.getCurrentPrice()).endDatetime(row.getEndDatetime())
                        .build());
        return PageResponse.from(page);
    }
}
