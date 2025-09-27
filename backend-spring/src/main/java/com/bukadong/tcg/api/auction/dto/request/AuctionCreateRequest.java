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
    @NotBlank(message = "경매 제목은 필수입니다.")
    @Size(min = 1, max = 40)
    private String title;

    @Schema(description = "카테고리 대분류 ID", example = "1000")
    @NotNull(message = "카테고리 대분류 ID는 필수입니다.")
    private Long categoryMajorId;

    @Schema(description = "카테고리 중분류 ID", example = "10000")
    @NotNull(message = "카테고리 중분류 ID는 필수입니다.")
    private Long categoryMediumId;

    @Schema(description = "카드 ID", example = "123")
    @NotNull(message = "카드 ID는 필수입니다.")
    private Long cardId;

    @Schema(description = "외부 AI 등급 해시", example = "ai_2fa1c5...") // UNIQUE
    @NotBlank(message = "외부 AI 등급 해시는 필수입니다.")
    private String gradeHash;

    @Schema(description = "실물 NFT 카드의 tokenId", example = "100020000")
    private Long tokenId;

    @Schema(description = "상세 설명(LONGTEXT)", example = "상세 설명입니다.")
    @NotBlank(message = "상세 설명은 필수입니다.")
    private String detail;

    @Schema(description = "입찰 단위", example = "0.1")
    @NotBlank(message = "입찰 단위는 필수입니다.")
    private String bidUnit; // Converter에서 AuctionBidUnit으로 변환

    @Schema(description = "시작가", example = "1.00000000")
    @NotNull(message = "시작가는 필수입니다.")
    @DecimalMin("0.00000000")
    @Digits(integer = 12, fraction = 8)
    private BigDecimal startPrice;

    @Schema(description = "입찰가(최초 현재가로 세팅원하면 null/미전송)", example = "1.00000000")
    @DecimalMin("0.00000000")
    @Digits(integer = 12, fraction = 8)
    private BigDecimal currentPrice;

    @Schema(description = "경매 시작일시", example = "2025-09-16T10:00:00")
    @NotNull(message = "경매 시작일시는 필수입니다.")
    private LocalDateTime startDatetime;

    @Schema(description = "경매 마감일시", example = "2025-09-20T10:00:00")
    @NotNull(message = "경매 마감일시는 필수입니다.")
    private LocalDateTime endDatetime;

    @Schema(description = "즉시구매 가능 여부", example = "false")
    private boolean buyNowFlag;

    @Schema(description = "(옵션) 즉시구매가", example = "10.00000000")
    @DecimalMin("0.00000000")
    @Digits(integer = 12, fraction = 8)
    private BigDecimal buyNowPrice;
}
