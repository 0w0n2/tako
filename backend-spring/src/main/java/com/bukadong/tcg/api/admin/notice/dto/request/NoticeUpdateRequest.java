package com.bukadong.tcg.api.admin.notice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공지 수정 요청
 * <P>
 * 제목/본문을 갱신합니다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Getter
@NoArgsConstructor
public class NoticeUpdateRequest {
    @Schema(description = "공지 제목(최대 50자)", example = "서비스 점검 안내(변경)")
    @NotBlank(message = "공지 제목은 필수입니다.")
    @Size(max = 50)
    private String title;

    @Schema(description = "공지 본문", example = "점검 시간이 03:30까지 연장되었습니다.")
    @NotBlank(message = "공지 본문은 필수입니다.")
    private String text;
}