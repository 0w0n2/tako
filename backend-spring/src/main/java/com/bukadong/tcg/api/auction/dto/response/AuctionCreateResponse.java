package com.bukadong.tcg.api.auction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 경매 생성 응답 DTO
 * <P>
 * 생성된 경매의 식별자 및 업로드된 이미지 URL(대표/전체) 등을 반환.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionCreateResponse {

    @Schema(description = "경매 ID")
    private Long auctionId;
}
