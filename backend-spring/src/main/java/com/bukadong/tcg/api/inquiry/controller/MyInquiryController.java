package com.bukadong.tcg.api.inquiry.controller;

import com.bukadong.tcg.api.inquiry.dto.response.InquiryListRow;
import com.bukadong.tcg.api.inquiry.service.InquiryQueryService;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 내 문의 목록 API
 * <P>
 * 전체 경매를 대상으로 현재 로그인 사용자가 작성한 문의만 페이징으로 조회합니다. 인증이 필수이며, 최신 작성 순(ID DESC)으로
 * 정렬합니다.
 * </P>
 * 
 * @PARAM page 페이지 번호(0-base)
 * @PARAM size 페이지 크기
 * @RETURN BaseResponse<Page<InquiryListRow>>
 */
@Tag(name = "Inquiries", description = "경매 문의/답변 API")
@RestController
@RequestMapping("/v1/auctions/inquiries")
@RequiredArgsConstructor
@Validated
public class MyInquiryController {

    private final InquiryQueryService inquiryQueryService;
    private final MemberQueryService memberQueryService;
    private final Logger log = LoggerFactory.getLogger(MyInquiryController.class);

    /**
     * 내 문의 목록 조회
     * <P>
     * 전체 경매에서 내가 작성한 문의만 반환합니다.
     * </P>
     * 
     * @PARAM page 페이지 번호(0-base)
     * @PARAM size 페이지 크기
     * @RETURN BaseResponse<Page<InquiryListRow>>
     */
    @Operation(summary = "내 문의 목록 조회", description = "모든 경매에서 현재 로그인 사용자가 작성한 문의만 페이징으로 반환합니다.")
    @GetMapping("/mine")
    public BaseResponse<Page<InquiryListRow>> listMyInquiries(
            @Parameter(description = "페이지 번호(0-base)") @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @Parameter(description = "페이지 크기") @RequestParam(name = "size", defaultValue = "20") @Min(1) int size,
            @AuthenticationPrincipal CustomUserDetails user) {
        log.debug("principal={}", (user == null ? "null" : user.getUuid()));

        if (user == null) {
            throw new BaseException(BaseResponseStatus.INQUIRY_NOT_LOGGED_IN);
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Long id = memberQueryService.getByUuid(user.getUuid()).getId();
        Page<InquiryListRow> result = inquiryQueryService.getMyList(id, pageable);
        return BaseResponse.onSuccess(result);
    }
}
