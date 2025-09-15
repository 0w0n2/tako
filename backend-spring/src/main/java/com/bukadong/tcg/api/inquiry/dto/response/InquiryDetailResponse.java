package com.bukadong.tcg.api.inquiry.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

import com.bukadong.tcg.api.inquiry.entity.Inquiry;
import com.bukadong.tcg.api.inquiry.entity.InquiryAnswer;
import com.bukadong.tcg.api.inquiry.util.TitleTrimer;

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

    /**
     * 문의 상세 응답 생성 팩토리
     * <P>
     * 비밀글 권한(canView)에 따라 본문/이미지/작성자/답변 정보를 마스킹하고, 제목은 존재 시 그대로, 없으면 본문을 기반으로
     * 생성합니다.
     * </P>
     * 
     * @PARAM inquiry 문의 엔티티
     * @PARAM answer 답변 엔티티(없으면 null)
     * @PARAM canView 비밀글 열람 가능 여부
     * @PARAM imageUrls presigned 이미지 URL 목록
     * @RETURN InquiryDetailResponse
     */
    public static InquiryDetailResponse of(Inquiry inquiry, InquiryAnswer answer, boolean canView, boolean hide,
            List<String> imageUrls) {

        // 제목
        String title;
        if (canView) {
            title = (inquiry.getTitle() != null && !inquiry.getTitle().isBlank()) ? inquiry.getTitle()
                    : TitleTrimer.trimAsTitle(inquiry.getContent());
        } else {
            title = "비밀글입니다.";
        }

        return InquiryDetailResponse.builder().id(inquiry.getId()).title(title)
                .content(hide ? null : inquiry.getContent()).imageUrls(imageUrls)
                .authorNickname(hide ? null : inquiry.getAuthor().getNickname())
                .createdAt(hide ? null : inquiry.getCreatedAt()).answerId(answer == null ? null : answer.getId())
                .answerContent((answer == null || hide) ? null : answer.getContent())
                .answerAuthorNickname(answer == null ? null : answer.getSeller().getNickname())
                .answerCreatedAt(answer == null ? null : answer.getCreatedAt()).build();
    }

}
