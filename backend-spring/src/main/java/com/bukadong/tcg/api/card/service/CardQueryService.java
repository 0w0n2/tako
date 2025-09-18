package com.bukadong.tcg.api.card.service;

import com.bukadong.tcg.api.card.dto.request.CardSearchRequest;
import com.bukadong.tcg.api.card.dto.response.CardDetailResponse;
import com.bukadong.tcg.api.card.dto.response.CardListRow;
import com.bukadong.tcg.api.card.repository.CardRepository;
import com.bukadong.tcg.api.card.repository.custom.CardRepositoryCustom;
import com.bukadong.tcg.api.card.repository.custom.CardSearchCond;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 카드 조회 서비스
 * <P>
 * 컨트롤러 검증 이후의 비즈니스/DB 의존 검증만 수행한다.
 * </P>
 * 
 * @PARAM request 검색 요청
 * @RETURN Page<CardListRow>
 */
@Service
@RequiredArgsConstructor
public class CardQueryService {

    private final CardRepository cardRepository;

    /**
     * 카드 검색
     * <P>
     * name 접두 검색 + description FULLTEXT 조합. 카테고리 필터 결합.
     * </P>
     */
    @Transactional(readOnly = true)
    public Page<CardListRow> search(CardSearchRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        CardSearchCond cond = CardSearchCond.builder().categoryMajorId(request.getCategoryMajorId())
                .categoryMediumId(request.getCategoryMediumId()).nameKeyword(request.getName())
                .descriptionKeyword(request.getDescription()).build();
        return cardRepository.search(cond, pageable);
    }

    /**
     * 카드 상세 조회 서비스
     * <P>
     * DB 의존 존재성 검증만 수행. 조회는 readOnly.
     * </P>
     * 
     * @PARAM id 카드 ID
     * @RETURN CardDetailResponse
     */
    @Transactional(readOnly = true)
    public CardDetailResponse getDetail(Long id) {
        CardDetailResponse dto = cardRepository.findDetailById(id);
        if (dto == null)
            throw new BaseException(BaseResponseStatus.CARD_NOT_FOUND);
        return dto;
    }
}
