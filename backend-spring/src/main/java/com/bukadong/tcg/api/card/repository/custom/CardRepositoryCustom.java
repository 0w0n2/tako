package com.bukadong.tcg.api.card.repository.custom;

import com.bukadong.tcg.api.card.dto.response.CardListRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 카드 커스텀 검색 리포지토리
 * <P>
 * QueryDSL 기반, 카운트 분리.
 * </P>
 * 
 * @PARAM cond 검색 조건
 * @PARAM pageable 페이지 정보
 * @RETURN Page<CardListRow>
 */
public interface CardRepositoryCustom {
    Page<CardListRow> search(CardSearchCond cond, Pageable pageable);
}
