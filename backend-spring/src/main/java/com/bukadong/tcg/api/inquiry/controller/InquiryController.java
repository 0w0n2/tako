package com.bukadong.tcg.api.inquiry.controller;

import com.bukadong.tcg.api.inquiry.dto.request.AnswerCreateRequest;
import com.bukadong.tcg.api.inquiry.dto.request.AnswerUpdateRequest;
import com.bukadong.tcg.api.inquiry.dto.request.InquiryCreateRequest;
import com.bukadong.tcg.api.inquiry.dto.request.InquiryUpdateRequest;
import com.bukadong.tcg.api.inquiry.dto.response.InquiryDetailResponse;
import com.bukadong.tcg.api.inquiry.dto.response.InquiryListRow;
import com.bukadong.tcg.api.inquiry.service.InquiryCommandService;
import com.bukadong.tcg.api.inquiry.service.InquiryQueryService;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
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
@RequestMapping("/v1/auctions/{auctionId}/inquiries")
@RequiredArgsConstructor
@Validated
public class InquiryController {

    private final InquiryQueryService inquiryQueryService;
    private final InquiryCommandService inquiryCommandService;
    private final MemberQueryService memberQueryService;

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
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Long viewerId = (user == null) ? null : memberQueryService.getIdByUuid(user.getUuid());
        Page<InquiryListRow> result = inquiryQueryService.getList(auctionId, viewerId, pageable);
        return BaseResponse.onSuccess(result);

    }

    // ---------- 상세 ----------
    /**
     * 문의 상세 조회
     * <P>
     * 비밀글이면 작성자/판매자만 본문/답변 내용 열람 가능.
     * </P>
     */
    @Operation(summary = "문의 상세 조회", description = "비밀글 권한 로직을 반영하여 상세 정보를 반환합니다.")
    @GetMapping("/{inquiryId}")
    public BaseResponse<InquiryDetailResponse> detail(
            @Parameter(description = "경매 ID", required = true) @PathVariable("auctionId") Long auctionId,
            @Parameter(description = "문의 ID", required = true) @PathVariable("inquiryId") Long inquiryId,
            @AuthenticationPrincipal CustomUserDetails user) {
        Long viewerId = (user == null) ? null : memberQueryService.getIdByUuid(user.getUuid());
        InquiryDetailResponse res = inquiryQueryService.getDetail(inquiryId, viewerId);
        return BaseResponse.onSuccess(res);

    }

    // ---------- 문의 작성/수정/삭제 ----------
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

    /**
     * 문의 수정(답변 전까지만)
     */
    @Operation(summary = "문의 수정", description = "답변이 작성되기 전까지 작성자가 문의를 수정할 수 있습니다.")
    @PatchMapping("/{inquiryId}")
    public BaseResponse<Void> update(@PathVariable("auctionId") Long auctionId,
            @PathVariable("inquiryId") Long inquiryId, @Valid @RequestBody InquiryUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        Member me = memberQueryService.getByUuid(user.getUuid());
        inquiryCommandService.updateInquiry(inquiryId, me, request);
        return BaseResponse.onSuccess();
    }

    /**
     * 문의 삭제(답변 전까지만)
     */
    @Operation(summary = "문의 삭제", description = "답변이 작성되기 전까지 작성자가 문의를 삭제할 수 있습니다.")
    @DeleteMapping("/{inquiryId}")
    public BaseResponse<Void> delete(@PathVariable("auctionId") Long auctionId,
            @PathVariable("inquiryId") Long inquiryId, @AuthenticationPrincipal CustomUserDetails user) {
        Member me = memberQueryService.getByUuid(user.getUuid());
        inquiryCommandService.deleteInquiry(inquiryId, me);
        return BaseResponse.onSuccess();
    }

    // ---------- 답변 작성/수정/삭제 (판매자) ----------
    /**
     * 답변 등록(판매자)
     */
    @Operation(summary = "답변 등록(판매자)", description = "판매자가 문의에 대한 답변을 등록합니다.")
    @PostMapping("/{inquiryId}/answers")
    public BaseResponse<Long> createAnswer(@PathVariable("auctionId") Long auctionId,
            @PathVariable("inquiryId") Long inquiryId, @Valid @RequestBody AnswerCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        Member me = memberQueryService.getByUuid(user.getUuid());
        Long id = inquiryCommandService.createAnswer(inquiryId, me, request);
        return BaseResponse.onSuccess(id);
    }

    /**
     * 답변 수정(판매자)
     */
    @Operation(summary = "답변 수정(판매자)", description = "판매자가 자신의 답변을 수정합니다.")
    @PatchMapping("/{inquiryId}/answers")
    public BaseResponse<Void> updateAnswer(@PathVariable("auctionId") Long auctionId,
            @PathVariable("inquiryId") Long inquiryId, @Valid @RequestBody AnswerUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        Member me = memberQueryService.getByUuid(user.getUuid());
        inquiryCommandService.updateAnswer(inquiryId, me, request);
        return BaseResponse.onSuccess();
    }

    /**
     * 답변 삭제(판매자)
     */
    @Operation(summary = "답변 삭제(판매자)", description = "판매자가 자신의 답변을 삭제합니다.")
    @DeleteMapping("/{inquiryId}/answers")
    public BaseResponse<Void> deleteAnswer(@PathVariable("auctionId") Long auctionId,
            @PathVariable("inquiryId") Long inquiryId, @AuthenticationPrincipal CustomUserDetails user) {
        Member me = memberQueryService.getByUuid(user.getUuid());
        inquiryCommandService.deleteAnswer(inquiryId, me);
        return BaseResponse.onSuccess();
    }
}
