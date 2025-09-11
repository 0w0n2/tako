package com.bukadong.tcg.api.category.entity;

import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;

import jakarta.persistence.*;
import lombok.*;

/**
 * 카테고리 중분류 엔티티
 * <p>
 * 대분류(CategoryMajor) 하위에 속하는 중분류 정보를 관리한다.
 * </p>
 * <ul>
 * <li>code, name 컬럼은 각각 고유(UNIQUE) 제약</li>
 * <li>category_major_id, name 컬럼에 인덱스 생성</li>
 * <li>생성/수정 일시는 BaseEntity로 자동 관리</li>
 * </ul>
 */
@Entity
@Table(name = "category_medium", uniqueConstraints = {
        // (대분류, 이름) 조합으로 유니크
        @UniqueConstraint(name = "uk_category_medium_major_name", columnNames = { "category_major_id",
                "name" }) }, indexes = {
                        @Index(name = "idx_category_medium_category", columnList = "category_major_id"), })
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

    /** 카테고리 이름 (고유) */
    @Column(name = "name", nullable = false, length = 30)
    private String name;

    /** 설명 */
    @Column(name = "description", nullable = false, length = 255)
    private String description;

    /** 생성 팩토리: 유효성 포함 */
    public static CategoryMedium of(CategoryMajor major, String name, String description) {
        if (major == null)
            throw new BaseException(BaseResponseStatus.CATEGORY_PARENT_NOT_FOUND);
        CategoryMedium m = new CategoryMedium();
        m.changeMajor(major);
        m.updateName(name);
        m.updateDescription(description);
        return m;
    }

    /** 상위 대분류 변경 */
    public void changeMajor(CategoryMajor newMajor) {
        if (newMajor == null)
            throw new BaseException(BaseResponseStatus.CATEGORY_PARENT_NOT_FOUND);
        this.categoryMajor = newMajor;
    }

    /** 의미 있는 변경 메서드들 (빈 문자열 불가) */
    public void update(String name, String description) {
        if (name != null)
            updateName(name);
        if (description != null)
            updateDescription(description);
    }

    public void updateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BaseException(BaseResponseStatus.INVALID_PARAMETER);
        }
        this.name = name.trim();
    }

    public void updateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new BaseException(BaseResponseStatus.INVALID_PARAMETER);
        }
        this.description = description.trim();
    }
}
