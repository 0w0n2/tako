package com.bukadong.tcg.api.auction.converter;

import com.bukadong.tcg.api.auction.dto.projection.AuctionListProjection;
import com.bukadong.tcg.api.auction.dto.response.AuctionListItemResponse;
import com.bukadong.tcg.api.media.service.MediaUrlService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 경매 목록 행 → 외부 응답 DTO 컨버터
 * <P>
 * 대표 이미지 URL은 반드시 MediaUrlService.getPresignedImageUrls(...)를 통해 presign된 URL을
 * 사용한다.
 * </P>
 * 
 * @PARAM row 내부 행 DTO
 * @PARAM mediaUrlService 미디어 URL 서비스
 * @PARAM ttl 프리사인 URL TTL
 * @RETURN 외부 응답 DTO
 */
public final class AuctionListConverter {

    private AuctionListConverter() {
    }

    public static AuctionListItemResponse toItem(AuctionListProjection row, MediaUrlService mediaUrlService,
            Duration ttl) {
        return toItem(row, mediaUrlService, ttl, false);
    }

    /**
     * 경매 목록 행 → 응답 DTO 변환(위시 여부 포함)
     * <P>
     * 대표 이미지 key를 presign하여 URL로 치환, 남은초는 0 미만이면 0으로 보정.
     * </P>
     * 
     * @PARAM row 프로젝션
     * @PARAM mediaUrlService presign 서비스
     * @PARAM ttl presign TTL
     * @PARAM wished 로그인 회원의 위시 여부
     * @RETURN AuctionListItemResponse
     */
    public static AuctionListItemResponse toItem(AuctionListProjection row, MediaUrlService mediaUrlService,
            Duration ttl, boolean wished) {
        String primaryUrl = null;
        if (row.primaryImageKey() != null && !row.primaryImageKey().isBlank()) {
            primaryUrl = mediaUrlService.getPresignedUrl(row.primaryImageKey(), ttl);
        }
        long remainingSeconds = Duration.between(LocalDateTime.now(ZoneOffset.UTC), row.endDatetime()).getSeconds();
        if (remainingSeconds < 0)
            remainingSeconds = 0;

        return new AuctionListItemResponse(row.id(), row.grade(), row.title(), row.currentPrice(), row.bidCount(),
                remainingSeconds, primaryUrl, wished);
    }
}
