package com.bukadong.tcg.api.admin.notice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공지 생성 요청
 * <P>
 * 제목/본문을 포함합니다. 제목은 최대 50자, 본문은 필수입니다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Getter
@NoArgsConstructor
public class NoticeCreateRequest {

    @Schema(description = "공지 제목(최대 50자)", example = "서비스 점검 안내")
    @NotBlank
    @Size(max = 50)
    private String title;

    @Schema(description = "공지 본문", example = "9/20 02:00~03:00 점검 예정입니다.")
    @NotBlank
    private String text;
}