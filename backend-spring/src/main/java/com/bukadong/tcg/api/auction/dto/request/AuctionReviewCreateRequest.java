package com.bukadong.tcg.api.auction.dto.request;

import com.bukadong.tcg.api.auction.entity.CardCondition;
import com.bukadong.tcg.api.auction.entity.DescriptionMatch;
import com.bukadong.tcg.api.auction.entity.PriceSatisfaction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuctionReviewCreateRequest {
    @Schema(description = "경매 ID", example = "123")
    @NotNull
    private Long auctionId;

    @Schema(description = "카드 상태", example = "NORMAL")
    @NotNull
    private CardCondition cardCondition;

    @Schema(description = "가격 만족도", example = "VERY_GOOD_DEAL")
    @NotNull
    private PriceSatisfaction priceSatisfaction;

    @Schema(description = "설명 일치도", example = "EXACT")
    @NotNull
    private DescriptionMatch descriptionMatch;

    @Schema(description = "별점(1~5)", example = "5")
    @Min(1)
    @Max(5)
    private int star;

    @Schema(description = "후기 내용(선택)", example = "빠른 거래 감사합니다.")
    private String reviewText;
}
