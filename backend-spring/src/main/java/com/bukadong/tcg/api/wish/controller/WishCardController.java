package com.bukadong.tcg.api.wish.controller;

import com.bukadong.tcg.api.wish.dto.response.WishCardListRow;
import com.bukadong.tcg.api.wish.service.WishCardCommandService;
import com.bukadong.tcg.api.wish.service.WishCardQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * 관심 카드 API
 * <P>
 * 카드 관심 추가/삭제/조회. 인증된 회원 기준으로 동작.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN BaseResponse
 */
@Tag(name = "Wish", description = "관심 카드/경매 추가/삭제/조회 API")
@RestController
@RequestMapping("/v1/wishes")
@RequiredArgsConstructor
@Validated
public class WishCardController {

    private final WishCardCommandService wishCardCommandService;
    private final WishCardQueryService wishCardQueryService;
    private final MemberQueryService memberQueryService;

    /**
     * 관심 카드 추가
     * <P>
     * 카드 ID로 관심 등록한다.
     * </P>
     * 
     * @PARAM cardId 카드 ID
     * @RETURN BaseResponse<Void>
     */
    @Operation(summary = "관심 카드 추가", description = "카드 ID로 관심 목록에 등록합니다.")
    @PostMapping("/cards/{cardId}")
    public BaseResponse<Void> add(
            @Parameter(description = "카드 ID", required = true) @PathVariable("cardId") @Min(1) Long cardId,
            @AuthenticationPrincipal CustomUserDetails user) {
        Long memberId = memberQueryService.getByUuid(user.getUuid()).getId();
        wishCardCommandService.add(memberId, cardId);
        return BaseResponse.onSuccess();
    }

    /**
     * 관심 카드 삭제
     * <P>
     * 카드 ID로 관심 해제한다.
     * </P>
     * 
     * @PARAM cardId 카드 ID
     * @RETURN BaseResponse<Void>
     */
    @Operation(summary = "관심 카드 삭제", description = "카드 ID로 관심 목록에서 해제합니다.")
    @DeleteMapping("/cards/{cardId}")
    public BaseResponse<Void> remove(
            @Parameter(description = "카드 ID", required = true) @PathVariable("cardId") @Min(1) Long cardId,
            @AuthenticationPrincipal CustomUserDetails user) {
        Long memberId = memberQueryService.getByUuid(user.getUuid()).getId();
        wishCardCommandService.remove(memberId, cardId);
        return BaseResponse.onSuccess();
    }

    /**
     * 내 관심 카드 목록 조회
     * <P>
     * id, name, cardImage를 페이지로 반환.
     * </P>
     * 
     * @PARAM page 페이지(0-base)
     * @PARAM size 페이지 크기(1~100)
     * @RETURN BaseResponse<PageResponse<WishCardListRow>>
     */
    @Operation(summary = "내 관심 카드 목록", description = "본인이 관심으로 추가한 카드 목록을 조회합니다.")
    @GetMapping("/cards")
    public BaseResponse<PageResponse<WishCardListRow>> listMy(
            @Parameter(description = "페이지 번호(0-base)", required = false) @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @Parameter(description = "페이지 크기(1~100)", required = false) @RequestParam(name = "size", defaultValue = "20") @Min(1) int size,
            @AuthenticationPrincipal CustomUserDetails user) {
        if (size > 100)
            size = 100;
        Long memberId = memberQueryService.getByUuid(user.getUuid()).getId();
        Page<WishCardListRow> result = wishCardQueryService.listMy(memberId, PageRequest.of(page, size));
        return BaseResponse.onSuccess(PageResponse.from(result));
    }
}
