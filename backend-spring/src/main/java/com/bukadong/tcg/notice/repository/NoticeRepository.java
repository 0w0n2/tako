package com.bukadong.tcg.notice.repository;

import com.bukadong.tcg.notice.entity.Notice;
import org.springframework.data.jpa.repository.*;

/**
 * 공지사항 엔티티용 JPA 레포지토리.
 * <p>
 * 기본 CRUD 및 페이징/정렬 메서드를 제공한다.
 * </p>
 */
public interface NoticeRepository extends JpaRepository<Notice, Long>, NoticeRepositoryCustom {
    // 기본 CRUD + QueryDSL 커스텀 기능 포함
}
