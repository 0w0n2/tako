package com.bukadong.tcg.api.inquiry.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 답변 등록 요청(판매자)
 * <P>
 * 본문 1000자 이내
 * </P>
 * 
 * @PARAM content 답변 본문
 * @RETURN 없음
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerCreateRequest {

    @Schema(description = "답변 본문(1000자 이내)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 1000)
    private String content;
}
