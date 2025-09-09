package com.bukadong.tcg.notice.controller;

import com.bukadong.tcg.common.base.BaseResponse;
import com.bukadong.tcg.common.dto.PageResponse;
import com.bukadong.tcg.notice.dto.response.NoticeDetailDto;
import com.bukadong.tcg.notice.dto.response.NoticeSummaryDto;
import com.bukadong.tcg.notice.service.NoticeService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 공지사항 조회 API (공개)
 * <p>
 * 공지사항 목록과 상세 정보를 제공한다.
 * </p>
 */
@RestController
@RequestMapping("/v1/notices")
@RequiredArgsConstructor
@Validated
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 공지사항 목록 조회 (페이지네이션 포함)
     * <p>
     * 제목, 글쓴이, 조회수, 생성일을 반환한다.
     * </p>
     *
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return PageResponse; NoticeSummaryDto;
     */
    @GetMapping
    public BaseResponse<PageResponse<NoticeSummaryDto>> list(@RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        Page<NoticeSummaryDto> p = noticeService.getSummaryPage(page, size);
        return new BaseResponse<>(PageResponse.from(p));
    }

    /**
     * 공지사항 단건 조회
     * <p>
     * 제목, 내용, 글쓴이, 조회수, 생성일, 첨부파일을 반환한다.
     * </p>
     *
     * @param id 공지사항 ID (1 이상)
     * @return 공지사항 단건
     */
    @GetMapping("/{id}")
    public BaseResponse<NoticeDetailDto> get(@PathVariable @Min(1) Long id) {
        NoticeDetailDto notice = noticeService.getDetail(id);
        return new BaseResponse<>(notice);
    }
}
