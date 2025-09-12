package com.bukadong.tcg.api.inquiry.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 문의 상세 응답
 * <P>
 * 비밀글일 경우 권한 검증 후 본문/답변 노출. 이미지 URL/ID는 Media 연계로 제공.
 * </P>
 * 
 * @PARAM id 문의 ID
 * @PARAM title 제목(없으면 서버에서 생성한 값 가능)
 * @PARAM content 본문(권한 없으면 null)
 * @PARAM imageUrls 이미지 URL 목록(권한 없으면 빈 배열)
 * @PARAM authorNickname 작성자 닉네임(마스킹 아님, 상세에서는 원닉)
 * @PARAM createdAt 생성시각
 * @PARAM answerId 답변 ID(없으면 null)
 * @PARAM answerContent 답변 본문(권한 없으면 null)
 * @PARAM answerAuthorNickname 답변자 닉네임
 * @PARAM answerCreatedAt 답변 작성시각
 * @RETURN 없음
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryDetailResponse {

    private Long id;
    private String title;
    private String content;
    private List<String> imageUrls;
    private String authorNickname;
    private LocalDateTime createdAt;

    private Long answerId;
    private String answerContent;
    private String answerAuthorNickname;
    private LocalDateTime answerCreatedAt;
}
