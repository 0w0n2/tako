package com.bukadong.tcg.api.media.policy;

import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.auction.repository.AuctionRepository;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 경매 미디어 정책
 * <P>
 * 판매자만 첨부 추가/삭제 가능. (필요 시 진행 상태 제약 추가)
 * </P>
 */
@Component
@RequiredArgsConstructor
public class AuctionMediaPolicy implements MediaPermissionPolicy {

    private final AuctionRepository auctionRepository;

    @Override
    public MediaType supports() {
        return MediaType.AUCTION_ITEM;
    }

    @Override
    public void checkCanAdd(MediaType type, Long ownerId, Member actor) {
        Auction auc = auctionRepository.findById(ownerId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));
        // 판매자 본인만 가능
        if (!auc.getMember().getId().equals(actor.getId()))
            throw new BaseException(BaseResponseStatus.MEDIA_FORBIDDEN);
        // 종료된 경매면 불가
        if (auc.isEnd())
            throw new BaseException(BaseResponseStatus.MEDIA_NOT_EDITABLE);
    }

    @Override
    public void checkCanDelete(MediaType type, Long ownerId, Long mediaId, Member actor) {
        checkCanAdd(type, ownerId, actor);
    }
}
