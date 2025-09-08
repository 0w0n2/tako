package com.bukadong.tcg.wish.entity;

import com.bukadong.tcg.card.entity.Card;
import com.bukadong.tcg.common.base.BaseEntity;
import com.bukadong.tcg.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

/**
 * 카드 찜하기 엔티티
 *
 * <p>
 * 회원이 특정 카드를 찜했는지 여부를 저장한다.
 * </p>
 *
 * <ul>
 * <li>(member_id, card_id) 복합 고유 제약</li>
 * <li>member_id, card_id, (member_id, wish_flag) 인덱스</li>
 * </ul>
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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishCard extends BaseEntity {

    /** 찜 ID (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 카드 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false, foreignKey = @ForeignKey(name = "FK_wish_card_card"))
    private Card card;

    /** 회원 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "FK_wish_card_member"))
    private Member member;

    /** 찜 여부 */
    @Column(name = "wish_flag", nullable = false)
    private boolean wishFlag;
}
