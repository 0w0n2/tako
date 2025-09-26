package com.bukadong.tcg.api.auction.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bukadong.tcg.api.auction.dto.request.AuctionReviewCreateRequest;
import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.auction.entity.AuctionReview;
import com.bukadong.tcg.api.auction.repository.AuctionRepository;
import com.bukadong.tcg.api.auction.repository.AuctionReviewRepository;
import com.bukadong.tcg.api.delivery.entity.Delivery;
import com.bukadong.tcg.api.delivery.entity.DeliveryStatus;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.repository.MemberRepository;
import com.bukadong.tcg.api.trust.service.MemberTrustService;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewCommandService {

    private final AuctionRepository auctionRepository;
    private final AuctionReviewRepository auctionReviewRepository;
    private final MemberRepository memberRepository;
    private final MemberTrustService memberTrustService;

    /**
     * 구매자가 구매확정한 경매에 대해 1회 리뷰 작성
     */
    @Transactional
    public Long createReview(Long writerMemberId, AuctionReviewCreateRequest req) {
        Member writer = memberRepository.findById(writerMemberId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_USER));
        Auction auction = auctionRepository.findById(req.getAuctionId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.AUCTION_NOT_FOUND));

        // 낙찰자만 작성 가능
        if (auction.getWinnerMemberId() == null || !auction.getWinnerMemberId().equals(writerMemberId)) {
            throw new BaseException(BaseResponseStatus.DELIVERY_FORBIDDEN_NOT_WINNER);
        }

        // 배송이 구매확정 상태(Confirmed)인지 체크 -> 리뷰 허용
        Delivery delivery = auction.getDelivery();
        if (delivery == null || delivery.getStatus() != DeliveryStatus.CONFIRMED) {
            throw new BaseException(BaseResponseStatus.DELIVERY_NOT_ARRIVED);
        }

        // 중복 리뷰 방지 (Member x Auction 유니크)
        Optional<AuctionReview> existing = auctionReviewRepository.findByMember_IdAndAuction_Id(writerMemberId,
                auction.getId());
        if (existing.isPresent()) {
            throw new BaseException(BaseResponseStatus.REVIEW_ALREADY_EXISTS);
        }

        AuctionReview saved = auctionReviewRepository
                .save(AuctionReview.builder().member(writer).auction(auction).cardCondition(req.getCardCondition())
                        .priceSatisfaction(req.getPriceSatisfaction()).descriptionMatch(req.getDescriptionMatch())
                        .star(req.getStar()).reviewText(req.getReviewText()).build());

        // 판매자(경매 등록자)의 신뢰도 업데이트 (카드 상태, 가격 만족도는 제외 규칙)
        Long sellerId = auction.getMember().getId();
        memberTrustService.updateOnReview(sellerId, req.getDescriptionMatch(), req.getStar());

        return saved.getId();
    }
}
