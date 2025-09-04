package com.bukadong.tcg.notice.service;

import com.bukadong.tcg.common.base.BaseResponseStatus;
import com.bukadong.tcg.common.exception.BaseException;
import com.bukadong.tcg.notice.entity.Notice;
import com.bukadong.tcg.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 공지사항 조회 비즈니스 로직.
 *
 * <p>
 * - 페이징 목록 조회
 * - 단건 조회(+존재하지 않을 경우 예외 변환)
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    /**
     * 공지사항 페이지 조회.
     *
     * @param page 0부터 시작하는 페이지 번호
     * @param size 페이지 크기(최대치 제한 등 정책 적용 가능)
     * @return Page 객체
     */
    public Page<Notice> getPage(int page, int size) {
        // [중요 로직] 과도한 size 요청 방지(예: 최대 100)
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return noticeRepository.findAll(PageRequest.of(safePage, safeSize));
    }

    /**
     * 공지사항 단건 조회.
     *
     * @param id 공지사항 ID
     * @return Notice 엔티티
     * @throws BaseException NOT_FOUND: 존재하지 않을 때
     */
    public Notice getById(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));
    }
}
