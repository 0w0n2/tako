package com.bukadong.tcg.api.auction.dto.response;

import com.bukadong.tcg.api.auction.entity.CardCondition;
import com.bukadong.tcg.api.auction.entity.DescriptionMatch;
import com.bukadong.tcg.api.auction.entity.PriceSatisfaction;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 경매 후기 응답 DTO
 * <P>
 * 회원이 받은 후기 정보를 간략히 전달한다.
 * </P>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionReviewResponse {

    @Schema(description = "후기 ID", example = "10")
    private Long id;

    @Schema(description = "경매 ID", example = "123")
    private Long auctionId;

    @Schema(description = "작성자 닉네임 (일부 마스킹)", example = "nick***")
    private String nickname;

    @Schema(description = "후기 내용", example = "빠른 거래 감사합니다.")
    private String reviewText;

    @Schema(description = "카드 상태", example = "NORMAL")
    private CardCondition cardCondition;

    @Schema(description = "가격 만족도", example = "VERY_GOOD_DEAL")
    private PriceSatisfaction priceSatisfaction;

    @Schema(description = "설명 일치도", example = "EXACT")
    private DescriptionMatch descriptionMatch;

    @Schema(description = "별점 (1~5)", example = "5")
    private int star;

    @Schema(description = "생성 일시 (ISO-8601)", example = "2025-09-11T12:30:45")
    private LocalDateTime createdAt;
}
