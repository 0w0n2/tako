package com.bukadong.tcg.notice.controller;

import com.bukadong.tcg.common.base.BaseResponse;
import com.bukadong.tcg.common.dto.PageResponse;
import com.bukadong.tcg.notice.entity.Notice;
import com.bukadong.tcg.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 공지사항 조회 API (공개)
 *
 */
@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

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

        Page<Notice> p = noticeService.getPage(page, size);
        return new BaseResponse<>(PageResponse.from(p));
    }

    /**
     * 공지사항 단건 조회
     *
     * @param id 공지사항 ID
     * @return 공지사항 단건
     */
    @GetMapping("/{id}")
    public BaseResponse<Notice> get(@PathVariable Long id) {
        Notice notice = noticeService.getById(id);
        return new BaseResponse<>(notice);
    }
}
