package com.bukadong.tcg.notice.repository;

import com.bukadong.tcg.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

/**
 * 공지사항 엔티티용 JPA 레포지토리.
 *
 * <p>
 * 기본 CRUD 및 페이징/정렬 메서드를 제공한다.
 * </p>
 */
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    /**
     * 공지 목록 조회 시 작성자까지 함께 로드하여 N+1을 방지한다.
     *
     * @param pageable 페이지 정보
     * @return Page of Notice
     */
    @EntityGraph(attributePaths = "author")
    Page<Notice> findAllBy(Pageable pageable);

    /**
     * 조회수 증가 (단일 UPDATE 쿼리)
     *
     * @param id 공지 ID
     * @return 업데이트된 행 수(0 또는 1)
     */
    @Modifying(clearAutomatically = false, flushAutomatically = false)
    @Query("update Notice n set n.viewCount = n.viewCount + 1 where n.id = :id")
    int incrementViewCount(@org.springframework.data.repository.query.Param("id") Long id);
}
