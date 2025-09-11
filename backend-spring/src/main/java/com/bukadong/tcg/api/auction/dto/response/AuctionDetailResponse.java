package com.bukadong.tcg.api.auction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 경매 상세 응답 DTO
 * <P>
 * 경매 정보, 카드 정보, 7일 시세(일자별 min/max/avg), 입찰 히스토리, 이미지 URL 목록을 포함한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 컨트롤러에서 BaseResponse로 감싸 반환
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuctionDetailResponse {

    @Schema(description = "경매 정보")
    private AuctionInfo auction;

    @Schema(description = "카드 정보")
    private CardInfo card;

    @Schema(description = "최근 7일 시세(일자별 min/max/avg)")
    private List<DailyPriceLine> weeklyPrices;

    @Schema(description = "입찰 히스토리")
    private List<BidHistoryItem> history;

    @Schema(description = "경매 이미지 URL 목록")
    private List<String> imageUrls;

    @Schema(description = "판매자 정보")
    private SellerInfo seller;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuctionInfo {
        private Long id;
        private String title;
        private String detail;
        private String grade;
        private String code;
        private BigDecimal startPrice;
        private BigDecimal currentPrice;
        private BigDecimal bidUnit;
        private LocalDateTime startDatetime;
        private LocalDateTime endDatetime;
        private boolean end;
        private boolean buyNowFlag;
        private BigDecimal buyNowPrice;
        private boolean extensionFlag;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CardInfo {
        private Long categoryMajorId;
        private String categoryMajorName;
        private Long categoryMediumId;
        private String categoryMediumName;
        private String cardName;
        private String cardDescription;
        private String attribute;
        private String rarity;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailyPriceLine {
        private LocalDate date;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private BigDecimal avgPrice;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BidHistoryItem {
        private LocalDateTime createdAt;
        private BigDecimal bidPrice;
        private String bidderNickname;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SellerInfo {
        private Long id;
        private String nickname;
        private Long reviewCount;
        private Double reviewStarAvg;
        private String profileImageUrl;
    }
}
