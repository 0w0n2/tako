package com.bukadong.tcg.category.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 카테고리
 *
 * 스키마 자동 생성 시 적용되는 @Table 메타데이터:
 * - @UniqueConstraint uk_category_code : code 컬럼 고유 제약 생성
 * - @Index idx_category_name : name 인덱스 생성
 */
@Entity
@Table(name = "category", uniqueConstraints = {
        @UniqueConstraint(name = "uk_category_code", columnNames = "code")
}, indexes = {
        @Index(name = "idx_category_name", columnList = "name")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    /** ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 카테고리 코드 */
    @Column(name = "code", nullable = false, length = 20) // 고유 제약은 @Table.uniqueConstraints로 관리
    private String code;

    /** 카테고리 이름 */
    @Column(name = "name", nullable = false, length = 30)
    private String name;

    /** 설명 */
    @Column(name = "description", nullable = false, length = 100)
    private String description;

    /** 대표 이미지 URL */
    @Column(name = "img_url", length = 200)
    private String imgUrl;
}
