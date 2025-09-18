package com.bukadong.tcg.api.wish.controller;

import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.api.wish.dto.response.WishAuctionListRow;
import com.bukadong.tcg.api.wish.service.WishAuctionCommandService;
import com.bukadong.tcg.api.wish.service.WishAuctionQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.common.dto.PageResponse;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 위시 경매 API
 * <P>
 * 목록 조회/추가/삭제를 제공한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Tag(name = "Wish")
@RestController
@RequestMapping("/v1/wishes/auctions")
@RequiredArgsConstructor
@Validated
public class WishAuctionController {

    private final WishAuctionQueryService wishAuctionQueryService;
    private final WishAuctionCommandService wishAuctionCommandService;
    private final MemberQueryService memberQueryService;

    /**
     * 내 관심 경매 목록
     * <P>
     * 페이지네이션으로 반환.
     * </P>
     * 
     * @PARAM page 페이지(0-base)
     * @PARAM size 사이즈(1~100 권장)
     * @PARAM memberId 회원 ID(추후 Security에서 추출)
     * @RETURN BaseResponse<PageResponse<WishAuctionListRow>>
     */
    @Operation(summary = "내 관심 경매 목록", description = "관심 등록한 경매를 페이지로 조회합니다.")
    @GetMapping
    public BaseResponse<PageResponse<WishAuctionListRow>> list(
            @Parameter(description = "페이지 인덱스(0-base)", required = false) @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @Parameter(description = "페이지 크기", required = false) @RequestParam(name = "size", defaultValue = "20") @Min(1) int size,
            @AuthenticationPrincipal CustomUserDetails user) {
        if (size > 100)
            size = 100;
        Long memberId = memberQueryService.getByUuid(user.getUuid()).getId();
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "endDatetime").and(Sort.by("auctionId").descending()));
        PageResponse<WishAuctionListRow> result = wishAuctionQueryService.list(memberId, pageable);
        return BaseResponse.onSuccess(result);
    }

    /**
     * 관심목록 추가(멱등)
     * <P>
     * 이미 있으면 유지.
     * </P>
     * 
     * @PARAM id 경매 ID
     * @PARAM memberId 회원 ID(추후 Security에서 추출)
     * @RETURN BaseResponse<Void>
     */
    @Operation(summary = "관심 경매 추가", description = "경매를 관심목록에 추가합니다.")
    @PostMapping("/{id}")
    public BaseResponse<Void> add(@Parameter(description = "경매 ID", required = true) @PathVariable("id") Long auctionId,
            @AuthenticationPrincipal CustomUserDetails user) {
        Long memberId = memberQueryService.getByUuid(user.getUuid()).getId();
        wishAuctionCommandService.add(memberId, auctionId);
        return BaseResponse.onSuccess();
    }

    /**
     * 관심목록 삭제(멱등)
     * <P>
     * 없어도 성공으로 처리.
     * </P>
     * 
     * @PARAM id 경매 ID
     * @PARAM memberId 회원 ID(추후 Security에서 추출)
     * @RETURN BaseResponse<Void>
     */
    @Operation(summary = "관심 경매 삭제", description = "관심목록에서 해당 경매를 제거합니다.")
    @DeleteMapping("/{id}")
    public BaseResponse<Void> remove(
            @Parameter(description = "경매 ID", required = true) @PathVariable("id") Long auctionId,
            @AuthenticationPrincipal CustomUserDetails user) {
        Long memberId = memberQueryService.getByUuid(user.getUuid()).getId();
        wishAuctionCommandService.remove(memberId, auctionId);
        return BaseResponse.onSuccess();
    }
}
