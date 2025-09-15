package com.bukadong.tcg.api.inquiry.controller;

import com.bukadong.tcg.api.inquiry.dto.response.InquiryListRow;
import com.bukadong.tcg.api.inquiry.service.InquiryQueryService;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 경매 문의/답변 API
 * <P>
 * 문의 등록/수정/삭제, 답변 등록/수정/삭제, 목록/상세 조회 제공.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN BaseResponse로 래핑된 결과
 */
@Tag(name = "Inquiries", description = "경매 문의/답변 API")
@RestController
@RequestMapping("/v1/inquiries/auctions/{auctionId}")
@RequiredArgsConstructor
@Validated
public class GetListController {

    private final InquiryQueryService inquiryQueryService;
    private final MemberQueryService memberQueryService;
    private final Logger log = LoggerFactory.getLogger(GetListController.class);

    // ---------- 목록 ----------
    /**
     * 문의 목록 조회
     * <P>
     * 비밀글은 제목만 '비밀글입니다.'로 노출.
     * </P>
     */
    @Operation(summary = "문의 목록 조회", description = "경매에 등록된 문의를 페이징으로 조회합니다.")
    @GetMapping
    public BaseResponse<Page<InquiryListRow>> list(
            @Parameter(description = "경매 ID", required = true) @PathVariable("auctionId") Long auctionId,
            @Parameter(description = "페이지 번호(0-base)") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails user) {
        log.debug("principal={}", (user == null ? "null" : user.getUuid()));
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Long viewerId = (user == null) ? null : memberQueryService.getByUuid(user.getUuid()).getId();
        Page<InquiryListRow> result = inquiryQueryService.getList(auctionId, viewerId, pageable);
        return BaseResponse.onSuccess(result);
    }
}
