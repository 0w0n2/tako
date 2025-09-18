package com.bukadong.tcg.api.wish.repository.auction;

import com.bukadong.tcg.api.auction.entity.QAuction;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.entity.QMedia;
import com.bukadong.tcg.api.wish.dto.response.WishAuctionListRow;
import com.bukadong.tcg.api.wish.entity.QWishAuction;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * WishAuction 커스텀 리포지토리 구현
 * <P>
 * wa.auctionId = auction.id, 대표 이미지(media.type=AUCTION_ITEM, seq_no=1) LEFT
 * JOIN.
 * </P>
 * 
 * @PARAM memberId 회원 ID
 * @PARAM pageable 페이지 정보
 * @RETURN Page<WishAuctionListRow> (imageUrl 필드는 일단 s3key로 채워 서비스에서 presign 변환)
 */
@Repository
@RequiredArgsConstructor
public class WishAuctionRepositoryImpl implements WishAuctionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<WishAuctionListRow> findMyWishAuctionsRaw(Long memberId, Pageable pageable) {
        QWishAuction wa = QWishAuction.wishAuction;
        QAuction a = QAuction.auction;
        QMedia m = QMedia.media;

        List<WishAuctionListRow> content = queryFactory.select(Projections.constructor(WishAuctionListRow.class, a.id, // auctionId
                m.s3keyOrUrl, // imageUrl (임시: 키를 담고, 서비스에서 presign URL로 변환)
                a.title, // title
                a.currentPrice, // currentPrice
                a.endDatetime // endDatetime
        )).from(wa).join(a).on(a.id.eq(wa.auctionId)).leftJoin(m)
                .on(m.type.eq(MediaType.AUCTION_ITEM).and(m.ownerId.eq(a.id)).and(m.seqNo.eq(1)))
                .where(wa.memberId.eq(memberId).and(wa.wishFlag.isTrue())).orderBy(a.endDatetime.desc(), a.id.desc())
                .offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

        Long total = queryFactory.select(a.id.count()).from(wa).join(a).on(a.id.eq(wa.auctionId))
                .where(wa.memberId.eq(memberId).and(wa.wishFlag.isTrue())).fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
