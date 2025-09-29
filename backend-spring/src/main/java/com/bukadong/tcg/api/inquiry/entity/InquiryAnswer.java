package com.bukadong.tcg.api.inquiry.entity;

import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.global.common.base.BaseEntity;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;

import jakarta.persistence.*;
import lombok.*;

/**
 * 경매 문의 답변 엔티티
 * <P>
 * 판매자만 등록/수정/삭제 가능. 문의당 0~1개의 답변을 갖는다.
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
@Table(name = "auction_inquiry_answer", indexes = { @Index(name = "idx_answer_inquiry", columnList = "inquiry_id"),
        @Index(name = "idx_answer_seller", columnList = "seller_id") })
public class InquiryAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 대상 문의 (1:1) */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inquiry_id", unique = true)
    private Inquiry inquiry;

    /** 답변자(판매자) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id")
    private Member seller;

    /** 답변 본문(최대 1000자) */
    @Column(nullable = false, length = 1000)
    private String content;

    public void updateContent(String content) {
        if (content == null || content.isBlank())
            throw new BaseException(BaseResponseStatus.INQUIRY_NO_CONTENT);
        this.content = content;
    }
}
