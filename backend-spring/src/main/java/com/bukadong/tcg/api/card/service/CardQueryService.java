package com.bukadong.tcg.api.card.service;

import com.bukadong.tcg.api.card.dto.request.CardSearchRequest;
import com.bukadong.tcg.api.card.dto.response.CardDetailResponse;
import com.bukadong.tcg.api.card.dto.response.CardListRow;
import com.bukadong.tcg.api.card.repository.CardRepository;
import com.bukadong.tcg.api.card.repository.custom.CardSearchCond;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.service.MediaUrlService;
import com.bukadong.tcg.api.wish.repository.card.WishCardRepository;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;

import lombok.RequiredArgsConstructor;

import java.time.Duration;

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
@Transactional(readOnly = true)
public class CardQueryService {

    private final CardRepository cardRepository;
    private final MediaUrlService mediaUrlService;
    private final WishCardRepository wishCardRepository;

    /**
     * 카드 검색
     * <P>
     * name 접두 검색 + description FULLTEXT 조합. 카테고리 필터 결합.
     * </P>
     */
    public Page<CardListRow> search(CardSearchRequest request, Long memberId) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        CardSearchCond cond = CardSearchCond.builder().categoryMajorId(request.getCategoryMajorId())
                .categoryMediumId(request.getCategoryMediumId()).nameKeyword(request.getName())
                .descriptionKeyword(request.getDescription()).build();
        return cardRepository.search(cond, pageable, memberId);
    }

    /**
     * 카드 상세 조회 서비스
     * <P>
     * DB 의존 존재성 검증만 수행. + 이미지 URL 주입 조회는 readOnly.
     * </P>
     * 
     * @PARAM id 카드 ID
     * @RETURN CardDetailResponse
     */
    public CardDetailResponse getDetail(Long cardId, Long memberId) {
        CardDetailResponse dto = cardRepository.findDetailById(cardId);
        if (dto == null)
            throw new BaseException(BaseResponseStatus.CARD_NOT_FOUND);
        dto.setImageUrls(mediaUrlService.getPresignedImageUrls(MediaType.CARD, cardId, Duration.ofMinutes(5)));
        boolean wished = false;
        if (memberId != null) {
            wished = wishCardRepository.existsByMemberIdAndCardIdAndWishFlagTrue(memberId, cardId);
        }
        dto.setWished(wished);
        return dto;
    }
}
