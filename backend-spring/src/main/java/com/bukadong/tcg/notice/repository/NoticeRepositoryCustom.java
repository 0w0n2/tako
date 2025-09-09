package com.bukadong.tcg.notice.repository;

/**
 * 공지사항 커스텀 레포지토리 (QueryDSL)
 * <p>
 * 표준 JpaRepository로 어려운 쿼리/업데이트를 QueryDSL로 정의한다.
 * </p>
 */
public interface NoticeRepositoryCustom {

    /**
     * 조회수 증가 (단일 UPDATE 쿼리)
     *
     * @param id 공지 ID
     * @return 업데이트된 행 수(0 또는 1)
     */
    int incrementViewCount(Long id);

}
