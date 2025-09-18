package com.bukadong.tcg.api.wish.repository;

import com.bukadong.tcg.api.card.entity.QCard;
import com.bukadong.tcg.api.wish.dto.response.WishCardListRow;
import com.bukadong.tcg.api.wish.entity.QWishCard;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * WishCard 커스텀 리포지토리 구현
 */
@Repository
@RequiredArgsConstructor
public class WishCardRepositoryImpl implements WishCardRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<WishCardListRow> findMyWishCards(Long memberId, Pageable pageable) {
        QWishCard wc = QWishCard.wishCard;
        QCard c = QCard.card;

        List<WishCardListRow> content = queryFactory
                .select(Projections.constructor(WishCardListRow.class, c.id.as("cardId"), c.name,
                        // cardImage는 서비스에서 채움(null placeholder)
                        // QueryDSL로는 null literal을 넣기 어렵기 때문에 빈 문자열로 넣고 이후 교체
                        // 하지만 Lombok 빌더/생성자에 맞추기 위해 null로 전달: Projections.constructor가 null 허용
                        c.name.stringValue().nullif(c.name.stringValue()).as("cardImage")))
                .from(wc).join(c).on(c.id.eq(wc.cardId)).where(wc.memberId.eq(memberId).and(wc.wishFlag.isTrue()))
                .orderBy(c.id.desc()).offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

        Long total = queryFactory.select(c.id.count()).from(wc).join(c).on(c.id.eq(wc.cardId))
                .where(wc.memberId.eq(memberId).and(wc.wishFlag.isTrue())).fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
