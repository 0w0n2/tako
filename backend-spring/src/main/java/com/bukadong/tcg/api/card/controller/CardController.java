package com.bukadong.tcg.api.card.controller;

import com.bukadong.tcg.api.card.dto.request.CardSearchRequest;
import com.bukadong.tcg.api.card.dto.response.CardListRow;
import com.bukadong.tcg.api.card.service.CardQueryService;
import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
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
    public BaseResponse<PageResponse<CardListRow>> search(@ParameterObject @Valid CardSearchRequest request) {

        Page<CardListRow> page = cardQueryService.search(request);
        return BaseResponse.onSuccess(PageResponse.from(page));
    }
}
