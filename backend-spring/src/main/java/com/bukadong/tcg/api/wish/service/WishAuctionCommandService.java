package com.bukadong.tcg.api.wish.service;

import com.bukadong.tcg.api.wish.entity.WishAuction;
import com.bukadong.tcg.api.wish.repository.auction.WishAuctionRepository;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 위시 경매 명령 서비스
 * <P>
 * 추가/삭제를 처리한다. 중복 추가는 허용(멱등), 삭제도 멱등.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Service
@RequiredArgsConstructor
public class WishAuctionCommandService {

    private final WishAuctionRepository wishAuctionRepository;

    /**
     * 관심 경매 추가 (upsert)
     * <P>
     * 행이 없으면 생성, 있으면 wishFlag=true로 갱신.
     * </P>
     * 
     * @PARAM memberId 회원 ID
     * @PARAM auctionId 경매 ID
     * @RETURN 없음
     */
    @Transactional
    public void add(Long memberId, Long auctionId) {
        WishAuction row = wishAuctionRepository.findByMemberIdAndAuctionId(memberId, auctionId)
                .orElseGet(() -> WishAuction.create(memberId, auctionId));
        row.enable();
        wishAuctionRepository.save(row);
    }

    /**
     * 관심 경매 해제
     * <P>
     * 행이 있으면 wishFlag=false로 갱신. 없으면 no-op.
     * </P>
     * 
     * @PARAM memberId 회원 ID
     * @PARAM auctionId 경매 ID
     * @RETURN 없음
     */
    @Transactional
    public void remove(Long memberId, Long auctionId) {
        wishAuctionRepository.findByMemberIdAndAuctionId(memberId, auctionId).ifPresent(WishAuction::disable);
    }
}
