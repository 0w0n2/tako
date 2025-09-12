package com.bukadong.tcg.api.inquiry.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 문의 목록 행 DTO
 * <P>
 * 요구사항의 목록 출력 형식 반영.
 * </P>
 * 
 * @PARAM id 문의 ID
 * @PARAM answerIds 답변 ID 배열(0 또는 1개)
 * @PARAM title 일부 내용으로 만든 제목(비밀글이면 '비밀글입니다.')
 * @PARAM maskedNickname 작성자 닉네임 일부 마스킹
 * @PARAM createdAt 작성일
 * @RETURN 없음
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryListRow {

    @Schema(description = "문의 ID")
    private Long id;

    @Schema(description = "답변 ID 0 또는 1개)")
    private Long answerId;

    @Schema(description = "제목(비밀글일 경우 '비밀글입니다.') ")
    private String title;

    @Schema(description = "작성자 닉네임(마스킹)")
    private String maskedNickname;

    @Schema(description = "작성일")
    private LocalDateTime createdAt;
}
