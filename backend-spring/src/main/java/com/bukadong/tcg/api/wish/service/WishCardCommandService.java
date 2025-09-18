package com.bukadong.tcg.api.wish.service;

import com.bukadong.tcg.api.card.repository.CardRepository;
import com.bukadong.tcg.api.wish.entity.WishCard;
import com.bukadong.tcg.api.wish.repository.WishCardRepository;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관심 카드 등록/해제 커맨드 서비스
 * <P>
 * 컨트롤러에서 검증된 파라미터를 가정하고 DB 의존 검증만 수행.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Service
@RequiredArgsConstructor
public class WishCardCommandService {

    private final WishCardRepository wishCardRepository;
    private final CardRepository cardRepository;

    /**
     * 관심 등록 (upsert)
     * <P>
     * 행이 없으면 생성, 있으면 wishFlag=true로 갱신.
     * </P>
     * 
     * @PARAM memberId 회원 ID
     * @PARAM cardId 카드 ID
     * @RETURN 없음
     */
    @Transactional
    public void add(Long memberId, Long cardId) {
        // 존재성 검증(서비스 레이어 DB 의존 검증)
        if (!cardRepository.existsById(cardId)) {
            throw new BaseException(BaseResponseStatus.CARD_NOT_FOUND);
        }

        WishCard row = wishCardRepository.findByMemberIdAndCardId(memberId, cardId)
                .orElseGet(() -> WishCard.create(memberId, cardId));
        row.enable();
        wishCardRepository.save(row);
    }

    /**
     * 관심 해제
     * <P>
     * 행이 있으면 wishFlag=false로 갱신. 없으면 no-op.
     * </P>
     * 
     * @PARAM memberId 회원 ID
     * @PARAM cardId 카드 ID
     * @RETURN 없음
     */
    @Transactional
    public void remove(Long memberId, Long cardId) {
        wishCardRepository.findByMemberIdAndCardId(memberId, cardId).ifPresent(WishCard::disable);
        // JPA dirty checking
    }
}
