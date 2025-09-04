package com.bukadong.tcg.review.entity;

import java.time.LocalDateTime;

import com.bukadong.tcg.auction.entity.Auction;
import com.bukadong.tcg.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

/**
 * 거래 후기
 *
 * 스키마 자동 생성 시 적용되는 @Table 메타데이터:
 * - @UniqueConstraint uk_review_member_auction : (member_id, auction_id) 복합 고유
 * 제약 생성
 * - @Index idx_review_member : member_id 인덱스 생성
 * - @Index idx_review_auction : auction_id 인덱스 생성
 */
@Entity
@Table(name = "review", uniqueConstraints = {
                @UniqueConstraint(name = "uk_review_member_auction", columnNames = { "member_id", "auction_id" })
}, indexes = {
                @Index(name = "idx_review_member", columnList = "member_id"),
                @Index(name = "idx_review_auction", columnList = "auction_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

        /** PK(Key) */
        @Id
        @Column(name = "`Key`", length = 255)
        private String key;

        /** 작성자 */
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "member_id", nullable = false)
        private Member writer;

        /** 대상 경매 */
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "auction_id", nullable = false)
        private Auction auction;

        /** 후기 내용 */
        @Column(name = "review_text", nullable = false, length = 100)
        private String reviewText;

        /** 별점 0~10 */
        @Column(name = "star", nullable = false)
        private int star;

        /** 생성 일시 */
        @Column(name = "created_at", nullable = false)
        private LocalDateTime createdAt;

        @PrePersist
        void onCreate() {
                LocalDateTime now = LocalDateTime.now();
                if (createdAt == null)
                        createdAt = now;
        }
}
