package com.bukadong.tcg.card.entity;

import com.bukadong.tcg.category.entity.CategoryMajor;
import com.bukadong.tcg.category.entity.CategoryMedium;
import com.bukadong.tcg.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 카드 마스터 엔티티
 *
 * <p>
 * 카드 기본 정보를 저장한다.
 * </p>
 *
 * <ul>
 * <li>카테고리 대분류, 중분류와 연관</li>
 * <li>code는 고유 제약 조건</li>
 * <li>attribute는 ROCK / PAPER / SCISSORS 중 하나 선택 (nullable)</li>
 * </ul>
 */
@Entity
@Table(name = "card", uniqueConstraints = {
                @UniqueConstraint(name = "uk_card_code", columnNames = "code")
}, indexes = {
                @Index(name = "idx_card_category_major", columnList = "category_major_id"),
                @Index(name = "idx_card_category_medium", columnList = "category_medium_id")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card extends BaseEntity {

        /** 카드 ID (PK) */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        /** 카테고리 대분류 */
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "category_major_id", nullable = false, foreignKey = @ForeignKey(name = "FK_card_category_major"))
        private CategoryMajor categoryMajor;

        /** 카테고리 중분류 */
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "category_medium_id", nullable = false, foreignKey = @ForeignKey(name = "FK_card_category_medium"))
        private CategoryMedium categoryMedium;

        /** 카드 코드 (고유) */
        @Column(name = "code", length = 30)
        private String code;

        /** 카드 이름 */
        @Column(name = "name", nullable = false, length = 30)
        private String name;

        /** 카드 설명 */
        @Column(name = "description", nullable = false, length = 100)
        private String description;

        /** 속성 (ROCK, PAPER, SCISSORS) */
        @Enumerated(EnumType.STRING)
        @Column(name = "attribute", length = 20, nullable = true)
        private Attribute attribute;
}
