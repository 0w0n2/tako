package com.bukadong.tcg.api.wish.service;

import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.service.MediaUrlService;
import com.bukadong.tcg.api.wish.dto.response.WishCardListRow;
import com.bukadong.tcg.api.wish.repository.WishCardRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * 관심 카드 조회 서비스
 * <P>
 * 대표 이미지 URL은 Media 테이블에서 seq_no ASC 첫 이미지로 해석한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Service
@RequiredArgsConstructor
public class WishCardQueryService {

    private final WishCardRepository wishCardRepository;
    private final MediaUrlService mediaUrlService;

    /**
     * 내 관심 카드 목록
     * <P>
     * N+1을 피하려면 썸네일을 별도 테이블/뷰에 캐시하는 전략을 고려할 수 있다.
     * </P>
     * 
     * @PARAM memberId 회원 ID
     * @PARAM pageable 페이지 정보
     * @RETURN Page<WishCardListRow>
     */
    @Transactional(readOnly = true)
    public Page<WishCardListRow> listMy(Long memberId, Pageable pageable) {
        Page<WishCardListRow> page = wishCardRepository.findMyWishCards(memberId, pageable);

        // 카드 대표 이미지(있으면 첫 번째) 주입: Presigned URL, 5분 TTL
        return page.map(row -> {
            String url = mediaUrlService.getPresignedImageUrls(MediaType.CARD, row.getCardId(), Duration.ofMinutes(5))
                    .stream().findFirst().orElse(null);
            return WishCardListRow.builder().cardId(row.getCardId()).name(row.getName()).cardImage(url).build();
        });
    }
}
