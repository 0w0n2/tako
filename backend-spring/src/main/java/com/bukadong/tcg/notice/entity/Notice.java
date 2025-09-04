package com.bukadong.tcg.notice.entity;

import com.bukadong.tcg.common.base.BaseEntity;
import com.bukadong.tcg.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

/**
 * 공지사항 (작성자는 관리자)
 *
 * 스키마 자동 생성 시 적용되는 @Table 메타데이터:
 * - @Index idx_notice_author : member_id 인덱스 생성
 * - @Index idx_notice_created : created_at 인덱스 생성
 * - @Index idx_notice_title : title 인덱스 생성(제목 검색/정렬용)
 */
@Entity
@Table(name = "notice", indexes = {
        @Index(name = "idx_notice_author", columnList = "member_id"),
        @Index(name = "idx_notice_created", columnList = "created_at"),
        @Index(name = "idx_notice_title", columnList = "title")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 작성자(관리자) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;

    /** 제목 */
    @Column(name = "title", nullable = false, length = 50)
    private String title;

    /** 내용 */
    @Column(name = "text", nullable = false, length = 100)
    private String text;
}
