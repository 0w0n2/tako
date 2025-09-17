package com.bukadong.tcg.api.notice.entity;

import com.bukadong.tcg.global.common.base.BaseEntity;
import com.bukadong.tcg.api.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

/**
 * 공지사항 엔티티
 * <p>
 * 관리자가 작성하는 공지사항 정보를 저장한다.
 * </p>
 * <ul>
 * <li>회원(member_id)와 연관 (작성자 FK)</li>
 * <li>제목(title), 본문(text), 조회수(view_count) 관리</li>
 * <li>제목/작성자/생성일 인덱스</li>
 * </ul>
 */
@Entity
@Table(name = "notice", indexes = { @Index(name = "idx_notice_author", columnList = "member_id"),
        @Index(name = "idx_notice_created", columnList = "created_at"),
        @Index(name = "idx_notice_title", columnList = "title") })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice extends BaseEntity {

    /** 공지사항 ID (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 작성자 (관리자) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "FK_notice_member"))
    private Member author;

    /** 제목 */
    @Column(name = "title", nullable = false, length = 50)
    private String title;

    /** 본문 내용 (긴 텍스트) */
    @Lob
    @Column(name = "text", nullable = false, columnDefinition = "LONGTEXT")
    private String text;

    /** 조회수 */
    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    /**
     * 공지 생성 팩토리
     * <P>
     * 작성자/제목/본문으로 새 Notice 인스턴스를 생성합니다. 조회수는 0으로 초기화합니다.
     * </P>
     * 
     * @PARAM author 작성자(Member, null 불가)
     * @PARAM title 공지 제목(공백 불가)
     * @PARAM text 공지 본문(공백 불가)
     * @RETURN 생성된 Notice
     */
    public static Notice create(Member author, String title, String text) {
        return Notice.builder().author(author).title(title).text(text).viewCount(0L).build();
    }

    /**
     * 공지 내용 수정
     * <P>
     * 제목/본문을 갱신합니다. 감사 필드(updatedAt)는 BaseEntity에 위임됩니다.
     * </P>
     * 
     * @PARAM title 수정할 제목(공백 불가)
     * @PARAM text 수정할 본문(공백 불가)
     * @RETURN 없음
     */
    public void update(String title, String text) {
        this.title = title;
        this.text = text;
    }

}
