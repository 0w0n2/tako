package com.bukadong.tcg.api.auction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Map;

/**
 * 경매 취소 가능 여부 응답
 * <P>
 * 취소 가능 플래그와 사유 코드를 함께 반환한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 취소 가능 여부/사유/메타
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionCancelCheckResponse {

    @Schema(description = "경매 ID", example = "1001")
    private Long auctionId;

    @Schema(description = "취소 가능 여부", example = "true")
    private boolean allowed;

    @Schema(description = "사유 코드 (OK/NOT_OWNER/ALREADY_ENDED/TIME_OVER/HAS_BID)", example = "OK")
    private String reason;

    /** 사유코드별 허용 여부 매핑 */
    private static final Map<String, Boolean> ALLOWED_MAP = Map.of("OK", true, "NOT_OWNER", false, "ALREADY_ENDED",
            false, "TIME_OVER", false, "HAS_BID", false);

    /**
     * 응답 정적 팩토리
     * <P>
     * 사유코드로 allowed를 자동 결정하고, 나머지 메타를 채워 반환한다.
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @PARAM reason 사유 코드
     * @PARAM now 응답 시각
     * @PARAM endAt 종료 시각
     * @PARAM owner 본인 경매 여부
     * @PARAM ended DB 종료 여부
     * @PARAM hasBid 입찰 존재 여부
     * @RETURN AuctionCancelCheckResponse
     */
    public static AuctionCancelCheckResponse of(Long auctionId, String reason) {
        boolean allowed = ALLOWED_MAP.getOrDefault(reason, false);
        return AuctionCancelCheckResponse.builder().auctionId(auctionId).allowed(allowed).reason(reason).build();
    }
}
