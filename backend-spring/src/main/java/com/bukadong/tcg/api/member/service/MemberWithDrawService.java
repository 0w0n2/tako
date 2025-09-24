package com.bukadong.tcg.api.member.service;

import com.bukadong.tcg.api.auction.repository.AuctionRepositoryCustom;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberWithDrawService {

    private final AuctionRepositoryCustom auctionRepositoryCustom;

    /**
     * 회원 탈퇴 가능 여부를 필요
     * 아래 조건 중 하나라도 해당되면 탈퇴할 수 없음
     * 1. 판매자로서 종료되지 않은 경매가 있는 경우
     * 2. 구매자로서 입찰한 경매 중 종료되지 않은 경매가 있는 경우
     * 3. 판매자 또는 낙찰자로서 거래가 완료(구매 확정)되지 않은 경매가 있는 경우
     *
     */
    @Transactional
    public void withDraw(Member member) {
        /* 1. 탈퇴 가능 여부 검증
         동시성 문제가 우려되어 검증 로직을 별도의 readOnly 트랜잭션으로 분리하진 않았음. */
        // 판매자 역할로 활성화된(종료되지 않은) 경매가 존재하는가
        if (auctionRepositoryCustom.existsActiveAuctionAsSeller(member.getId())) {
            throw new BaseException(BaseResponseStatus.CANNOT_WITHDRAW_AS_SELLER);
        }
        // 구매자 역할로 활성화된 경매에 입찰 내역이 있는가
        if (auctionRepositoryCustom.existsBidOnActiveAuction(member.getId())) {
            throw new BaseException(BaseResponseStatus.CANNOT_WITHDRAW_AS_BIDDER);
        }
        // 거래 미완료 내역이 있는가
        if (auctionRepositoryCustom.existsUnsettledAuctionAsParty(member.getId())) {
            throw new BaseException(BaseResponseStatus.CANNOT_WITHDRAW_UNSETTLED);
        }
        
        // 2. TODO: 필요시 연관된 테이블 데이터 처리(수정/삭제 등)

        // 3. 회원 탈퇴 (Soft Delete)
        member.changeSoftDeletedState(true);
    }
}
