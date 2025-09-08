package com.bukadong.tcg.category.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 카테고리 대분류 엔티티
 *
 * <p>
 * 카드/경매가 속하는 대분류 정보를 관리한다.
 * </p>
 *
 * <ul>
 * <li>ame 컬럼 고유(UNIQUE) 제약</li>
 * <li>name 컬럼에 조회 성능 향상을 위한 인덱스 생성</li>
 * <li>생성/수정 일시는 BaseEntity로 자동 관리</li>
 * </ul>
 */
@Entity
@Table(name = "category_major", uniqueConstraints = {
                @UniqueConstraint(name = "uk_category_major_name", columnNames = "name")
}, indexes = {
                @Index(name = "idx_category_major_name", columnList = "name")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryMajor {

        /** 대분류 ID (PK) */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        /** 카테고리 이름 (고유) */
        @Column(name = "name", nullable = false, length = 30)
        private String name;

        /** 설명 */
        @Column(name = "description", nullable = false, length = 255)
        private String description;
}
