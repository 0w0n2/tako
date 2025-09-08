package com.bukadong.tcg.category.entity;

import com.bukadong.tcg.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 카테고리 중분류 엔티티
 *
 * <p>
 * 대분류(CategoryMajor) 하위에 속하는 중분류 정보를 관리한다.
 * </p>
 *
 * <ul>
 * <li>code, name 컬럼은 각각 고유(UNIQUE) 제약</li>
 * <li>category_major_id, name 컬럼에 인덱스 생성</li>
 * <li>생성/수정 일시는 BaseEntity로 자동 관리</li>
 * </ul>
 */
@Entity
@Table(name = "category_medium", uniqueConstraints = {
                @UniqueConstraint(name = "uk_category_medium_code", columnNames = "code"),
                @UniqueConstraint(name = "uk_category_medium_name", columnNames = "name")
}, indexes = {
                @Index(name = "idx_category_medium_category", columnList = "category_major_id"),
                @Index(name = "idx_category_medium_name", columnList = "name")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryMedium {

        /** 중분류 ID (PK) */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        /** 대분류 (필수) */
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "category_major_id", nullable = false, foreignKey = @ForeignKey(name = "FK_category_medium_major"))
        private CategoryMajor categoryMajor;

        /** 카테고리 코드 (고유) */
        @Column(name = "code", nullable = false, length = 20)
        private String code;

        /** 카테고리 이름 (고유) */
        @Column(name = "name", nullable = false, length = 30)
        private String name;

        /** 설명 */
        @Column(name = "description", nullable = false, length = 255)
        private String description;
}
