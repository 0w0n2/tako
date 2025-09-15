package com.bukadong.tcg.api.auction.service;

import com.bukadong.tcg.api.auction.dto.request.BidCreateRequest;
import com.bukadong.tcg.api.auction.dto.response.BidResultResponse;
import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.auction.entity.AuctionBid;
import com.bukadong.tcg.api.auction.entity.AuctionBidStatus;
import com.bukadong.tcg.api.auction.repository.AuctionBidRepository;
import com.bukadong.tcg.api.auction.repository.AuctionLockRepository;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.service.MemberQueryService;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 입찰 생성 커맨드 서비스
 * <P>
 * DB 행락(PESSIMISTIC_WRITE)로 경매 1건을 직렬화해 동시성 안전하게 입찰을 반영한다.
 * </P>
 * 
 * @PARAM auctionId 경매 ID
 * @PARAM viewerUuid 인증 사용자 UUID
 * @PARAM request 입찰 생성 요청(bidPrice)
 * @RETURN BidResultResponse
 */
@Service
@RequiredArgsConstructor
public class BidCommandService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final MemberQueryService memberQueryService;
    private final AuctionLockRepository auctionLockRepository;
    private final AuctionBidRepository auctionBidRepository;

    /**
     * 입찰 생성 (행락 기반 직렬화)
     * <P>
     * 1) 경매 행락 획득 → 2) 상태/최소허용가 재검증 → 3) 입찰/경매 갱신 → 4) 커밋
     * </P>
     */
    @Transactional
    public BidResultResponse place(Long auctionId, String viewerUuid, BidCreateRequest request) {
        // 회원 조회
        Member me = memberQueryService.getByUuid(viewerUuid);

        // 경매 잠금 조회 (행락)
        Auction auction = auctionLockRepository.findByIdForUpdate(auctionId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.AUCTION_NOT_FOUND));

        // 본인 경매 입찰 불가
        if (auction.getMember() != null && auction.getMember().getId() != null
                && auction.getMember().getId().equals(me.getId())) {
            throw new BaseException(BaseResponseStatus.AUCTION_BID_FORBIDDEN);
        }

        // 상태/기간 재검증 (isEnd + 시작/종료 범위 체크)
        LocalDateTime now = LocalDateTime.now(KST);
        if (!auction.isRunningAt(now)) {
            // 시작 전이거나 종료 이후이거나, isEnd=true 인 경우
            throw new BaseException(BaseResponseStatus.AUCTION_NOT_RUNNING);
        }

        // 지갑 검증
        // TODO:잔액 검증 - 추후 지갑/정산 시스템 연동
        // boolean enough = walletService.hasEnough(me.getId(), request.getBidPrice());
        // if (!enough) throw new BaseException(BaseResponseStatus.CONFLICT);

        // 최소 허용가 검증(+배수 검증 필요 시 여기에 추가): currentPrice + bidUnit
        BigDecimal minAllowed = auction.getCurrentPrice().add(auction.getBidUnit().toBigDecimal());
        if (request.getBidPrice().compareTo(minAllowed) < 0) {
            // 경쟁 레이스로 현재가가 선점된 경우도 여기서 걸러짐
            throw new BaseException(BaseResponseStatus.AUCTION_BID_NOT_POSSIBLE_PRICE);
        }

        // 입찰 생성
        AuctionBid bid = AuctionBid.builder().auction(auction).member(me).bidPrice(request.getBidPrice())
                .status(AuctionBidStatus.VALID).build();
        auctionBidRepository.save(bid);

        // 경매 갱신 (현재가)
        auction.setCurrentPrice(request.getBidPrice());

        // flush는 트랜잭션 종료 시점에 일괄 처리됨
        return new BidResultResponse(bid.getId(), auction.getId(), auction.getCurrentPrice(), now, "ACCEPTED");
    }
}
