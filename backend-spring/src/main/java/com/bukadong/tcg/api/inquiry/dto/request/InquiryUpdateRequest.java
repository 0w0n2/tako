package com.bukadong.tcg.api.inquiry.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 문의 수정 요청(답변 등록 전까지 가능)
 * <P>
 * 제목/본문/비밀글 여부 변경. 이미지 첨부 변경은 별도 정책에 맞춰 서비스에서 처리.
 * </P>
 * 
 * @PARAM title 제목(선택)
 * @PARAM content 본문(필수, 1000자 이내)
 * @PARAM secret 비밀글 여부
 * @RETURN 없음
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryUpdateRequest {

    @Schema(description = "제목(선택, 100자 이내)")
    @Size(max = 100)
    private String title;

    @Schema(description = "본문(필수, 1000자 이내)")
    @NotBlank(message = "본문은 필수입니다.")
    @Size(max = 1000)
    private String content;

    @Schema(description = "비밀글 여부(선택). null이면 변경하지 않음", nullable = true)
    private Boolean secret;
}
