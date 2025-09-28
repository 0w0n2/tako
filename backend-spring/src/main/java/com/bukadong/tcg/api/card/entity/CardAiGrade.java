package com.bukadong.tcg.api.card.entity;

import com.bukadong.tcg.global.common.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 카드 AI 등급 엔티티
 * <P>
 * 외부 AI가 분석한 카드 컨디션 등급을 저장한다. hash는 외부 API가 반환하는 고유 식별자이며 UNIQUE.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Entity
@Table(name = "card_ai_grade", indexes = { @Index(name = "uk_card_ai_grade_hash", columnList = "hash", unique = true) })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardAiGrade extends BaseEntity {

    /** PK (BIGINT) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 컨디션 등급 (예: PS/NM 등 1~2자리) */
    @NotBlank(message = "컨디션 등급은 필수입니다.")
    @Size(min = 1, max = 2)
    @Column(name = "grade_code", nullable = false, length = 2)
    private String gradeCode;

    /** 외부 AI가 발급한 고유 해시 (UNIQUE) */
    @NotBlank(message = "해시는 필수입니다.")
    @Size(min = 10, max = 255)
    @Column(name = "hash", nullable = false, length = 255, unique = true)
    private String hash;

    /** (옵션) 실물카드 식별자 해시 — 운영 스키마와 협의 후 사용 */
    @Size(max = 255)
    @Column(name = "physical_card_hash", length = 255)
    private String physicalCardHash;
}
