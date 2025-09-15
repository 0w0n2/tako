package com.bukadong.tcg.api.auction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 경매 생성 요청 DTO
 * <P>
 * 이미지는 멀티파트로 별도 전달하며, 본 DTO는 메타데이터를 담는다.
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
public class AuctionCreateRequest {

    @Schema(description = "경매 제목", example = "블루아이즈 화이트 드래곤 1st")
    @NotBlank
    @Size(min = 1, max = 40)
    private String title;

    @Schema(description = "카테고리 대분류 ID", example = "1000")
    @NotNull
    private Long categoryMajorId;

    @Schema(description = "카테고리 중분류 ID", example = "10000")
    @NotNull
    private Long categoryMediumId;

    @Schema(description = "카드 ID", example = "123")
    @NotNull
    private Long cardId;

    @Schema(description = "외부 AI 등급 해시", example = "ai_2fa1c5...") // UNIQUE
    @NotBlank
    private String gradeHash;

    @Schema(description = "(옵션) 실물카드 해시 - 있으면 물리카드 매핑", example = "pc_b1a2c3...")
    private String physicalCardHash;

    @Schema(description = "상세 설명(LONGTEXT)", example = "상세 설명입니다.")
    @NotBlank
    private String detail;

    @Schema(description = "입찰 단위", example = "0.1")
    @NotBlank
    private String bidUnit; // Converter에서 AuctionBidUnit으로 변환

    @Schema(description = "시작가", example = "1.00000000")
    @NotNull
    @DecimalMin("0.00000000")
    @Digits(integer = 12, fraction = 8)
    private BigDecimal startPrice;

    @Schema(description = "입찰가(최초 현재가로 세팅원하면 null/미전송)", example = "1.00000000")
    @DecimalMin("0.00000000")
    @Digits(integer = 12, fraction = 8)
    private BigDecimal currentPrice;

    @Schema(description = "경매 시작일시", example = "2025-09-16T10:00:00")
    @NotNull
    private LocalDateTime startDatetime;

    @Schema(description = "경매 마감일시", example = "2025-09-20T10:00:00")
    @NotNull
    private LocalDateTime endDatetime;

    @Schema(description = "즉시구매 가능 여부", example = "false")
    private boolean buyNowFlag;

    @Schema(description = "(옵션) 즉시구매가", example = "10.00000000")
    @DecimalMin("0.00000000")
    @Digits(integer = 12, fraction = 8)
    private BigDecimal buyNowPrice;
}
