package com.bukadong.tcg.api.inquiry.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 문의 등록 요청
 * <P>
 * 제목(선택), 본문(필수, 1000자 이내), 비밀글 여부, 이미지 ID 배열(선택)
 * </P>
 * 
 * @PARAM title 선택 제목
 * @PARAM content 본문(필수)
 * @PARAM secret 비밀글 여부
 * @PARAM imageIds 첨부 이미지 Media ID 목록(선택)
 * @RETURN 없음
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryCreateRequest {

    @Schema(description = "제목(선택, 100자 이내)")
    @Size(max = 100)
    private String title;

    @Schema(description = "본문(필수, 1000자 이내)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "본문은 필수입니다.")
    @Size(max = 1000)
    private String content;

    @Schema(description = "비밀글 여부", defaultValue = "false")
    private boolean secret;
}
