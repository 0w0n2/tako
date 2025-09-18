package com.bukadong.tcg.api.card.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카드 상세 응답 DTO
 * <P>
 * 카드 단건 상세 조회 결과를 전달한다. enum 필드는 문자열로 직렬화한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Getter
@NoArgsConstructor
@Schema(name = "CardDetailResponse", description = "카드 상세 응답")
public class CardDetailResponse {

    @Schema(description = "카드 ID", example = "123")
    private Long id;

    @Schema(description = "대분류 ID", example = "10")
    private Long categoryMajorId;

    @Schema(description = "중분류 ID", example = "101")
    private Long categoryMediumId;

    @Schema(description = "카드 코드", example = "P-001")
    private String code;

    @Schema(description = "카드 이름", example = "Blue Dragon")
    private String name;

    @Schema(description = "카드 설명", example = "전설의 푸른 용")
    private String description;

    @Schema(description = "속성", example = "ROCK")
    private String attribute;

    @Schema(description = "희귀도", example = "RADIANT_RARE")
    private String rarity;

    @Schema(description = "이미지 URL 목록(대표=첫번째)", example = "[\"https://.../card/1/1.png\",\"https://.../card/1/2.png\"]")
    private List<String> imageUrls;

    /** 서비스에서 프리사인 URL 주입용 세터 (프로젝션과 충돌 방지 차원에서 최소 공개) */
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
