package com.bukadong.tcg.api.card.controller;

import com.bukadong.tcg.api.card.dto.request.CardSearchRequest;
import com.bukadong.tcg.api.card.dto.response.CardDetailResponse;
import com.bukadong.tcg.api.card.dto.response.CardListRow;
import com.bukadong.tcg.api.card.service.CardQueryService;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.api.popularity.service.PopularityService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.common.dto.PageResponse;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 카드 조회 컨트롤러
 * <P>
 * 검색/페이징 API. 컨트롤러는 얇게 유지하고 서비스에 위임.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN BaseResponse<PageResponse<CardListRow>>
 */
@Tag(name = "Cards", description = "카드 검색 API")
@RestController
@RequestMapping("/v1/cards")
@RequiredArgsConstructor
@Validated
public class CardController {

    private final CardQueryService cardQueryService;
    private final MemberQueryService memberQueryService;
    private final PopularityService popularityService;

    /**
     * 카드 검색
     * <P>
     * 카테고리 + 이름 접두 + 설명 FULLTEXT(ngram). 결과는 점수/ID로 정렬합니다.
     * </P>
     * 
     * @PARAM request CardSearchRequest (query 파라미터)
     * @RETURN BaseResponse<PageResponse<CardListRow>>
     */
    @Operation(summary = "카드 검색", description = "카테고리/이름/설명 조건으로 페이지 검색합니다.")
    @GetMapping
    public BaseResponse<PageResponse<CardListRow>> search(@ParameterObject @Valid CardSearchRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        Long memberId = (user == null) ? null : memberQueryService.getByUuid(user.getUuid()).getId();
        Page<CardListRow> page = cardQueryService.search(request, memberId);
        return BaseResponse.onSuccess(PageResponse.from(page));
    }

    /**
     * 카드 상세 조회
     * <P>
     * id로 단일 카드를 조회한다. 존재하지 않으면 NOT_FOUND 예외를 발생시킨다.
     * </P>
     * 
     * @PARAM id 카드 ID
     * @RETURN BaseResponse<CardDetailResponse>
     */
    @Operation(summary = "카드 상세 조회", description = "카드의 기본 메타(id, categoryMajorId, categoryMediumId, code, name, description, attribute, rarity)를 반환합니다.")
    @GetMapping("/{cardId}")
    public BaseResponse<CardDetailResponse> getDetail(
            @Parameter(name = "cardId", description = "카드 ID", required = true) @PathVariable("cardId") Long cardId,
            @AuthenticationPrincipal CustomUserDetails user) {
        Long memberId = (user == null) ? null : memberQueryService.getByUuid(user.getUuid()).getId();
        var resp = cardQueryService.getDetail(cardId, memberId);
        try {
            popularityService.recordCardDetailView(cardId);
        } catch (Exception ignore) {
            // 인기도 카운팅 실패는 본 요청 흐름에 영향 주지 않음
        }
        return BaseResponse.onSuccess(resp);
    }
}
