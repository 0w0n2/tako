package com.bukadong.tcg.card.entity;

import com.bukadong.tcg.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

/**
 * 실물 카드 엔티티
 *
 * <p>
 * 회원이 보유한 실물 카드를 관리한다.
 * </p>
 *
 * <ul>
 * <li>card_id: 카드 마스터와 연관</li>
 * <li>owner_member_id: 소유 회원</li>
 * <li>uuid: 실물 카드 식별용, 고유 제약</li>
 * </ul>
 */
@Entity
@Table(name = "physical_card", uniqueConstraints = {
                @UniqueConstraint(name = "uk_pcard_uuid", columnNames = "uuid")
}, indexes = {
                @Index(name = "idx_pcard_card", columnList = "card_id"),
                @Index(name = "idx_pcard_owner", columnList = "owner_member_id")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhysicalCard {

        /** 실물 카드 ID (PK) */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        /** 카드 마스터 */
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "card_id", nullable = false, foreignKey = @ForeignKey(name = "FK_physical_card_card"))
        private Card card;

        /** 소유 회원 */
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "owner_member_id", nullable = false, foreignKey = @ForeignKey(name = "FK_physical_card_owner"))
        private Member owner;

        /** 실물 카드 UUID (고유 식별자) */
        @Column(name = "uuid", nullable = false, length = 60, unique = true)
        private String uuid;
}
