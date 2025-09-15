package com.bukadong.tcg.api.inquiry.controller;

import com.bukadong.tcg.api.inquiry.dto.request.InquiryCreateRequest;
import com.bukadong.tcg.api.inquiry.service.InquiryCommandService;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
public class CreateController {
    private final InquiryCommandService inquiryCommandService;
    private final MemberQueryService memberQueryService;

    // ---------- 문의 작성 ----------
    /**
     * 문의 등록
     */
    @Operation(summary = "문의 등록", description = "사용자가 경매에 문의를 등록합니다.")
    @PostMapping
    public BaseResponse<Long> create(
            @Parameter(description = "경매 ID", required = true) @PathVariable("auctionId") Long auctionId,
            @Valid @RequestBody InquiryCreateRequest request, @AuthenticationPrincipal CustomUserDetails user) {
        Member me = memberQueryService.getByUuid(user.getUuid());
        Long id = inquiryCommandService.createInquiry(auctionId, me, request);
        return BaseResponse.onSuccess(id);
    }

}
