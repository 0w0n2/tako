package com.bukadong.tcg.card.entity;

import com.bukadong.tcg.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

/**
 * 실물 카드 정보
 *
 * 스키마 자동 생성 시 적용되는 @Table 메타데이터:
 * - @UniqueConstraint uk_pcard_uuid : uuid 컬럼 고유 제약 생성
 * - @Index idx_pcard_card : card_id 인덱스 생성
 * - @Index idx_pcard_owner : member_id 인덱스 생성
 */
@Entity
@Table(name = "physical_card", uniqueConstraints = {
                @UniqueConstraint(name = "uk_pcard_uuid", columnNames = "uuid")
}, indexes = {
                @Index(name = "idx_pcard_card", columnList = "card_id"),
                @Index(name = "idx_pcard_owner", columnList = "member_id")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhysicalCard {

        /** ID */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        /** 카드 마스터 */
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "card_id", nullable = false)
        private Card card;

        /** 소유 회원 */
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "member_id", nullable = false)
        private Member member;

        /** 실물 식별 UUID(선택) — 고유 제약은 @Table.uniqueConstraints로 관리 */
        @Column(length = 60)
        private String uuid;
}
