package com.bukadong.tcg.api.auction.dto.response;

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

    @Schema(description = "작성자 닉네임 (일부 마스킹)", example = "nick***")
    private String nickname;

    @Schema(description = "후기 내용", example = "빠른 거래 감사합니다.")
    private String reviewText;

    @Schema(description = "별점 (0~10)", example = "9")
    private int star;

    @Schema(description = "생성 일시 (ISO-8601)", example = "2025-09-11T12:30:45")
    private LocalDateTime createdAt;
}
