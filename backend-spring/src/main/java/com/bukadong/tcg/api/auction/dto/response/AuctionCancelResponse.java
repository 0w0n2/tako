package com.bukadong.tcg.api.auction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 경매 취소 결과
 * <P>
 * 누가 언제 취소했는지 반환한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 취소 결과 메타
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuctionCancelResponse {

    @Schema(description = "경매 ID", example = "1001")
    private Long auctionId;

    @Schema(description = "취소 주체(USER/ADMIN)", example = "USER")
    private String cancelledBy;

    @Schema(description = "취소 시각", example = "2025-09-16T15:01:00")
    private LocalDateTime cancelledAt;
}
