package com.bukadong.tcg.notice.controller;

import com.bukadong.tcg.common.base.BaseResponse;
import com.bukadong.tcg.common.base.BaseResponseStatus;
import com.bukadong.tcg.common.dto.PageResponse;
import com.bukadong.tcg.common.exception.BaseException;
import com.bukadong.tcg.notice.entity.Notice;
import com.bukadong.tcg.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 공지사항 조회 API (공개)
 *
 * <p>
 * BaseResponse / BaseException 체계를 따른다.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeRepository noticeRepository;

    /**
     * 공지사항 목록 조회 (페이지네이션 포함)
     *
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return PageResponse로 감싼 공지사항 목록
     */
    @GetMapping
    public BaseResponse<PageResponse<Notice>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Notice> p = noticeRepository.findAll(PageRequest.of(page, size));
        return new BaseResponse<>(PageResponse.from(p));
    }

    /**
     * 공지사항 단건 조회
     *
     * @param id 공지사항 ID
     * @return 공지사항 단건
     * @throws BaseException 존재하지 않는 경우
     */
    @GetMapping("/{id}")
    public BaseResponse<Notice> get(@PathVariable Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));
        return new BaseResponse<>(notice);
    }
}
