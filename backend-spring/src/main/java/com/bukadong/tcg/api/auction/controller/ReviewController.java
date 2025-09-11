package com.bukadong.tcg.api.auction.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bukadong.tcg.api.auction.dto.response.AuctionReviewResponse;
import com.bukadong.tcg.api.auction.service.ReviewQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Reviews", description = "회원 후기 API")
@RestController
@RequestMapping("/v1/members")
@RequiredArgsConstructor
@Validated
public class ReviewController {

    private final ReviewQueryService reviewQueryService;

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
    @GetMapping("/{id}/reviews")
    public BaseResponse<List<AuctionReviewResponse>> getReviews(
            @Parameter(description = "회원 ID", example = "1001") @PathVariable("id") Long id) {
        List<AuctionReviewResponse> result = reviewQueryService.getReviewsByMember(id);
        return BaseResponse.onSuccess(result);
    }
}