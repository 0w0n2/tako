package com.bukadong.tcg.api.inquiry.entity;

import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.global.common.base.BaseEntity;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import com.fasterxml.jackson.databind.JsonSerializable.Base;

import jakarta.persistence.*;
import lombok.*;

/**
 * 경매 문의 엔티티
 * <P>
 * 사용자가 경매에 남기는 문의. 비밀글 여부를 통해 공개 범위를 제어한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "auction_inquiry", indexes = { @Index(name = "idx_inquiry_auction", columnList = "auction_id"),
        @Index(name = "idx_inquiry_member", columnList = "member_id") })
public class Inquiry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 문의 작성자 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member author;

    /** 대상 경매 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auction_id")
    private Auction auction;

    /** 제목(선택). 없으면 콘텐츠 앞부분을 제목으로 활용 */
    @Column(length = 100)
    private String title;

    /** 본문(최대 1000자) */
    @Column(nullable = false, length = 1000)
    private String content;

    /** 비밀글 여부 */
    @Column(name = "is_secret", nullable = false)
    private boolean secret;

    public boolean isAuthor(Long memberId) {
        return this.author != null && this.author.getId().equals(memberId);
    }

    /**
     * 문의 수정 (답변 등록 전까지만)
     * <P>
     * PATCH 시맨틱: null은 미변경, 빈 문자열은 무시(미변경). 전달된 값만 갱신합니다.
     * </P>
     * 
     * @PARAM title 제목(선택, null/blank면 미변경)
     * @PARAM content 본문(선택, null/blank면 미변경)
     * @PARAM secret 비밀글 여부(선택, null이면 미변경)
     * @RETURN 없음
     */
    public void updateBeforeAnswered(String title, String content, Boolean secret) {
        if (title != null) {
            String t = title.trim();
            if (!t.isEmpty()) {
                this.title = t;
            }
        }
        if (content != null) {
            String c = content.trim();
            if (!c.isEmpty()) {
                this.content = c;
            }
        }
        if (secret != null) {
            this.secret = secret;
        }
    }

}
