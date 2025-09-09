package com.bukadong.tcg.api.notice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bukadong.tcg.api.notice.dto.response.NoticeDetailDto;
import com.bukadong.tcg.api.notice.dto.response.NoticeSummaryDto;

/**
 * 공지사항 커스텀 레포지토리 (QueryDSL)
 * <p>
 * 표준 JpaRepository로 어려운 쿼리/업데이트를 QueryDSL로 정의한다.
 * </p>
 */
public interface NoticeRepositoryCustom {

    /**
     * 공지사항 목록 페이징 조회 (작성자 포함 → DTO 변환)
     *
     * @param pageable 페이지 정보
     * @return NoticeSummaryDto 페이지
     */
    Page<NoticeSummaryDto> findSummaryPage(Pageable pageable);

    /**
     * 공지사항 단건 상세 조회 (작성자 + 첨부파일 포함 → DTO 변환)
     *
     * @param id 공지 ID
     * @return NoticeDetailDto
     */
    NoticeDetailDto findDetailDtoById(Long id);

    /**
     * 조회수 증가 (원자적 UPDATE)
     *
     * @param id 공지 ID
     * @return 업데이트된 행 수
     */
    int incrementViewCount(Long id);
}
