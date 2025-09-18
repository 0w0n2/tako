package com.bukadong.tcg.api.card.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 카드 검색 요청 DTO
 * <P>
 * 카테고리/이름 접두 검색 + 설명 전문검색(FULLTEXT) 조건을 전달한다.
 * </P>
 * 
 * @PARAM categoryMajorId 대분류 ID
 * @PARAM categoryMediumId 중분류 ID
 * @PARAM name 이름 접두 검색어 (LIKE 'name%')
 * @PARAM description 설명 검색어 (FULLTEXT ngram)
 * @PARAM page 페이지(0 기반)
 * @PARAM size 페이지 크기(1~100)
 * @RETURN 없음
 */
@Getter
@Setter // ]컨트롤러 쿼리파라미터 바인딩에 필요
@NoArgsConstructor // 바인딩용 기본 생성자
public class CardSearchRequest {

    @Schema(description = "대분류 ID", example = "1")
    private Long categoryMajorId;

    @Schema(description = "중분류 ID", example = "10")
    private Long categoryMediumId;

    @Schema(description = "카드명 전문검색(FULLTEXT ngram, 다단어 공백 구분)", example = "청룡 전설")
    @Size(max = 30)
    private String name;

    @Schema(description = "설명/이름 전문검색(FULLTEXT ngram, 다단어는 공백 구분)", example = "드래곤 공격력")
    @Size(max = 100)
    private String description;

    @Schema(description = "페이지 (0-base)", example = "0", defaultValue = "0")
    @Min(0)
    private int page = 0;

    @Schema(description = "페이지 크기", example = "20", defaultValue = "20")
    @Min(1)
    private int size = 20;
}
