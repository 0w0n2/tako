package com.bukadong.tcg.api.inquiry.entity;

import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.global.common.base.BaseEntity;
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

    public void updateBeforeAnswered(String title, String content, boolean secret) {
        // 간단 불변식 가드
        if (content == null || content.isBlank())
            throw new IllegalArgumentException("content is blank");
        this.title = (title != null && !title.isBlank()) ? title : this.title;
        this.content = content;
        this.secret = secret;
    }
}
