package com.bukadong.tcg.card.entity;

import com.bukadong.tcg.category.entity.Category;
import com.bukadong.tcg.common.base.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 카드 마스터
 *
 * 스키마 자동 생성 시 적용되는 @Table 메타데이터:
 * - @UniqueConstraint uk_card_code : code 컬럼 고유 제약 생성
 * - @Index idx_card_category : category_id 인덱스 생성
 */
@Entity
@Table(name = "card", uniqueConstraints = {
        @UniqueConstraint(name = "uk_card_code", columnNames = "code")
}, indexes = {
        @Index(name = "idx_card_category", columnList = "category_id")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card extends BaseEntity {

    /** ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 카테고리 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** 카드 코드 */
    @Column(nullable = false, length = 30) // 고유 제약은 @Table.uniqueConstraints로 관리
    private String code;

    /** 카드 희귀도 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rarity rarity;

    /** 카드 이름 */
    @Column(nullable = false, length = 30)
    private String name;

    /** 카드 설명 */
    @Column(nullable = false, length = 100)
    private String description;

    /** ' SCISSORS' → 'SCISSORS'로 정규화된 enum 사용 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Attribute attribute;
}
