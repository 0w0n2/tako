package com.bukadong.tcg.api.auction.converter;

import com.bukadong.tcg.api.auction.dto.projection.AuctionListProjection;
import com.bukadong.tcg.api.auction.dto.response.AuctionListItemResponse;
import com.bukadong.tcg.api.media.entity.MediaKind;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.service.MediaUrlService;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

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

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private AuctionListConverter() {
    }

    public static AuctionListItemResponse toItem(AuctionListProjection row, MediaUrlService mediaUrlService,
            Duration ttl) {
        // 남은 초 계산(KST 기준, 0 하한)
        long remainingSeconds = Math.max(0L,
                Duration.between(ZonedDateTime.now(KST), row.endDatetime().atZone(KST)).getSeconds());

        // 대표 이미지: 반드시 MediaUrlService 메서드를 통해 presign
        // 목록이 반환되므로 첫 번째 이미지를 대표로 사용 (없으면 null)
        String primaryUrl = mediaUrlService
                .getPresignedImageUrls(MediaType.AUCTION_ITEM, row.id(), MediaKind.IMAGE, ttl).stream().findFirst()
                .orElse(null);

        return new AuctionListItemResponse(row.id(), row.grade(), row.title(), row.currentPrice(), row.bidCount(),
                remainingSeconds, primaryUrl);
    }
}
