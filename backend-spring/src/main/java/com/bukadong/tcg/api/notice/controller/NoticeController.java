package com.bukadong.tcg.api.notice.controller;

import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.common.dto.PageResponse;
import com.bukadong.tcg.api.notice.dto.response.NoticeDetailDto;
import com.bukadong.tcg.api.notice.dto.response.NoticeSummaryDto;
import com.bukadong.tcg.api.notice.service.NoticeService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "Notices", description = "공지사항 목록 및 상세 조회 API")
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
    @Operation(summary = "공지사항 목록 조회", description = "페이지네이션된 공지사항 목록을 반환합니다.")
    @GetMapping
    public BaseResponse<PageResponse<NoticeSummaryDto>> list(
            @Parameter(description = "페이지 번호(0부터 시작)") @RequestParam(name = "page", defaultValue = "0") @PositiveOrZero int page,
            @Parameter(description = "페이지 크기") @RequestParam(name = "size", defaultValue = "20") @Min(1) int size) {
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
    @Operation(summary = "공지사항 단건 조회", description = "공지사항의 상세 정보를 반환합니다.")
    @GetMapping("/{noticeId}")
    public BaseResponse<NoticeDetailDto> get(
            @Parameter(description = "공지사항 ID(1 이상)") @PathVariable("noticeId") @Min(1) Long noticeId) {
        NoticeDetailDto notice = noticeService.getDetail(noticeId);
        return new BaseResponse<>(notice);
    }
}
