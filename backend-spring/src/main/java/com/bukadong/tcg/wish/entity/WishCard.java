package com.bukadong.tcg.wish.entity;

import com.bukadong.tcg.card.entity.Card;
import com.bukadong.tcg.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 카드 찜하기
 *
 * 스키마 자동 생성 시 적용되는 @Table 메타데이터:
 * - @UniqueConstraint uk_wish_card_member_card : (member_id, card_id) 복합 고유 제약
 * 생성
 * - @Index idx_wish_card_member : member_id 인덱스 생성
 * - @Index idx_wish_card_card : card_id 인덱스 생성
 * - @Index idx_wish_card_member_flag : (member_id, wish_flag) 복합 인덱스 생성
 */
@Entity
@Table(name = "wish_card", uniqueConstraints = {
        @UniqueConstraint(name = "uk_wish_card_member_card", columnNames = { "member_id", "card_id" })
}, indexes = {
        @Index(name = "idx_wish_card_member", columnList = "member_id"),
        @Index(name = "idx_wish_card_card", columnList = "card_id"),
        @Index(name = "idx_wish_card_member_flag", columnList = "member_id,wish_flag")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 카드 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    /** 회원 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** 찜 여부 */
    @Column(name = "wish_flag", nullable = false)
    private boolean wishFlag;

    /** 생성 일시 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 수정 일시 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null)
            createdAt = now;
        if (updatedAt == null)
            updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
