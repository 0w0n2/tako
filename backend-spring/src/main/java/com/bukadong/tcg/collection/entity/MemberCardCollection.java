package com.bukadong.tcg.collection.entity;

import com.bukadong.tcg.card.entity.PhysicalCard;
import com.bukadong.tcg.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

/**
 * 유저가 가진 실물 카드 목록
 *
 * 스키마 자동 생성 시 적용되는 @Table 메타데이터:
 * - @UniqueConstraint uk_ucdex_member_pcard : (member_id, physical_card_id) 복합
 * 고유 제약 생성
 * - @Index idx_ucdex_member : member_id 인덱스 생성
 * - @Index idx_ucdex_pcard : physical_card_id 인덱스 생성
 */
@Entity
@Table(name = "member_card_collection", uniqueConstraints = {
                @UniqueConstraint(name = "uk_ucdex_member_pcard", columnNames = { "member_id", "physical_card_id" })
}, indexes = {
                @Index(name = "idx_ucdex_member", columnList = "member_id"),
                @Index(name = "idx_ucdex_pcard", columnList = "physical_card_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberCardCollection {

        /** ID */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        /** 소유 회원 */
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "member_id", nullable = false)
        private Member member;

        /** 실물 카드 */
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "physical_card_id", nullable = false)
        private PhysicalCard physicalCard;
}
