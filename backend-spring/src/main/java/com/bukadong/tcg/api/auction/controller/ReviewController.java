package com.bukadong.tcg.api.auction.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bukadong.tcg.api.auction.dto.request.AuctionReviewCreateRequest;
import com.bukadong.tcg.api.auction.dto.response.AuctionReviewResponse;
import com.bukadong.tcg.api.auction.dto.request.MyReviewRole;
import com.bukadong.tcg.api.auction.dto.response.MyReviewItemResponse;
import com.bukadong.tcg.api.auction.service.ReviewCommandService;
import com.bukadong.tcg.api.auction.service.ReviewQueryService;
import com.bukadong.tcg.api.auction.service.MyReviewQueryService;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;

@Tag(name = "Reviews", description = "회원 후기 API")
@RestController
@RequestMapping("/v1/reviews")
@RequiredArgsConstructor
@Validated
public class ReviewController {

    private final ReviewQueryService reviewQueryService;
    private final ReviewCommandService reviewCommandService;
    private final MyReviewQueryService myReviewQueryService;
    private final MemberQueryService memberQueryService;

    /**
     * 회원이 받은 후기 목록 조회
     * <P>
     * 특정 회원이 받은 모든 후기를 반환한다.
     * </P>
     * 
     * @PARAM id 회원 ID
     * @RETURN BaseResponse<List<AuctionReviewResponse>>
     */
    @Operation(summary = "회원 후기 조회", description = "회원이 받은 모든 후기를 조회합니다.")
    @GetMapping("/{memberId}/reviews")
    public BaseResponse<List<AuctionReviewResponse>> getReviews(
            @Parameter(description = "회원 ID", example = "1001") @PathVariable("memberId") Long memberId) {
        List<AuctionReviewResponse> result = reviewQueryService.getReviewsByMember(memberId);
        return BaseResponse.onSuccess(result);
    }

    /**
     * 구매자 리뷰 등록 요청 본문은 템플릿 기반 선택/별점/텍스트
     */
    @Operation(summary = "구매자 리뷰 등록", description = "구매확정된 경매에 대해 구매자가 1회 리뷰를 등록합니다.")
    @PostMapping
    public BaseResponse<Long> createReview(@AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody AuctionReviewCreateRequest request) {
        Long writerMemberId = memberQueryService.getByUuid(user.getUuid()).getId();
        Long id = reviewCommandService.createReview(writerMemberId, request);
        return BaseResponse.onSuccess(id);
    }

    /**
     * 나의 리뷰 관련 목록 조회 (BUYER/SELLER, done 여부)
     * role=BUYER, done=false  -> 내가 작성해야 할 리뷰 대상 경매 목록
     * role=BUYER, done=true   -> 내가 작성한 리뷰가 포함된 경매 목록
     * role=SELLER, done=false -> 내가 리뷰를 받을 예정인 경매 목록(아직 미작성)
     * role=SELLER, done=true  -> 내가 받은 리뷰가 포함된 경매 목록
     */
    @Operation(summary = "내 리뷰 목록 조회", description = "role(BUYER/SELLER), done(true/false)에 따라 나의 리뷰 대상/완료 목록을 조회합니다.")
    @GetMapping("/me")
    public BaseResponse<List<MyReviewItemResponse>> getMyReviews(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "역할", example = "BUYER") @RequestParam("role") MyReviewRole role,
            @Parameter(description = "작성 완료 여부", example = "false") @RequestParam("done") boolean done) {
        Long meId = memberQueryService.getByUuid(user.getUuid()).getId();
        List<MyReviewItemResponse> result = myReviewQueryService.listMe(meId, role, done);
        return BaseResponse.onSuccess(result);
    }
}