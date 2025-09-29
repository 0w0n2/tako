package com.bukadong.tcg.api.auction.entity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.bukadong.tcg.api.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 경매 후기 엔티티
 * <p>
 * 회원이 특정 경매에 대해 남긴 후기 정보를 저장한다.
 * </p>
 * <ul>
 * <li>회원과 경매 조합은 하나의 후기만 작성 가능 (복합 유니크 제약)</li>
 * <li>별점(star)은 1~5 범위</li>
 * <li>생성일(created_at)은 저장 시 자동으로 현재 시각으로 설정</li>
 * </ul>
 */
@Entity
@Table(name = "auction_review", uniqueConstraints = {
        @UniqueConstraint(name = "uk_auction_review_member_auction", columnNames = { "member_id",
                "auction_id" }) }, indexes = { @Index(name = "idx_auction_review_member", columnList = "member_id"),
                        @Index(name = "idx_auction_review_auction", columnList = "auction_id") })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionReview {

    /** 후기 ID (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 작성자 (회원) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "FK_auction_review_member"))
    private Member member;

    /** 대상 경매 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auction_id", nullable = false, foreignKey = @ForeignKey(name = "FK_auction_review_auction"))
    private Auction auction;

    /** 후기 내용 (선택) */
    @Column(name = "review_text", nullable = true, length = 255)
    private String reviewText;

    /** 카드 상태 (필수): 사용감이 많아요/보통이에요/새 거 같아요 */
    @Enumerated(EnumType.STRING)
    @Column(name = "card_condition", nullable = false, length = 20)
    private CardCondition cardCondition;

    /** 가격 만족도 (필수): 너무 잘 샀어요/보통이에요/비싸게 샀어요 */
    @Enumerated(EnumType.STRING)
    @Column(name = "price_satisfaction", nullable = false, length = 20)
    private PriceSatisfaction priceSatisfaction;

    /** 설명 일치도 (필수): 정확히 일치/거의 비슷/차이 있음 */
    @Enumerated(EnumType.STRING)
    @Column(name = "description_match", nullable = false, length = 20)
    private DescriptionMatch descriptionMatch;

    /** 별점 (1~5) */
    @Min(1)
    @Max(5)
    @Column(name = "star", nullable = false)
    private int star;

    /** 생성 일시 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 저장 시 생성일을 현재 시간으로 설정 */
    @PrePersist
    void prePersist() {
        // 별점은 1~5 허용
        if (star < 1 || star > 5) {
            star = Math.max(1, Math.min(5, star));
        }
        if (reviewText == null) {
            reviewText = "";
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now(ZoneOffset.UTC);
        }
    }
}
