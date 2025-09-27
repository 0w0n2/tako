package com.bukadong.tcg.api.member.service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.auction.entity.AuctionReview;
import com.bukadong.tcg.api.bid.repository.AuctionBidRepository;
import com.bukadong.tcg.api.auction.repository.AuctionRepository;
import com.bukadong.tcg.api.auction.repository.AuctionReviewRepository;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.service.MediaUrlService;
import com.bukadong.tcg.api.member.dto.response.PublicMemberProfileResponse;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.repository.MemberRepository;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublicMemberProfileService {

    private final MemberRepository memberRepository;
    private final AuctionRepository auctionRepository;
    private final AuctionBidRepository auctionBidRepository;
    private final AuctionReviewRepository auctionReviewRepository;
    private final MediaUrlService mediaUrlService;

    @Transactional(readOnly = true)
    public PublicMemberProfileResponse getPublicProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));

        String profileUrl = mediaUrlService
                .getPrimaryImageUrl(MediaType.MEMBER_PROFILE, member.getId(), Duration.ofMinutes(5)).orElse(null);
        String backgroundUrl = mediaUrlService
                .getPrimaryImageUrl(MediaType.MEMBER_BACKGROUND, member.getId(), Duration.ofMinutes(5)).orElse(null);

        // 판매 경매: 진행중 경매 최근 10개 기준 (요구 명세에 명확치 않아 합리적 가정)
        List<Auction> auctions = auctionRepository
                .findByMember_IdOrderByIdDesc(memberId, PageRequest.of(0, 10))
                .getContent();

        List<PublicMemberProfileResponse.SellAuctionRow> sellRows = auctions.stream()
                .map(this::mapAuctionToRow)
                .collect(Collectors.toList());

        List<AuctionReview> reviews = auctionReviewRepository.findByAuctionMemberId(memberId);
        List<PublicMemberProfileResponse.ReviewRow> reviewRows = reviews.stream()
                .map(this::mapReviewToRow)
                .collect(Collectors.toList());

        return PublicMemberProfileResponse.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .introduction(member.getIntroduction())
                .profileImageUrl(profileUrl)
                .backgroundImageUrl(backgroundUrl)
                .sellAuctions(sellRows)
                .reviews(reviewRows)
                .build();
    }

    private PublicMemberProfileResponse.SellAuctionRow mapAuctionToRow(Auction a) {
        long remaining = 0L;
        if (!a.isEnd() && a.getEndDatetime() != null) {
            long nowEpoch = Instant.now().getEpochSecond();
            long endEpoch = a.getEndDatetime().toEpochSecond(ZoneOffset.UTC);
            remaining = Math.max(0, endEpoch - nowEpoch);
        }
        long bidCount = auctionBidRepository.countByAuction_Id(a.getId());
        String imgUrl = mediaUrlService
                .getPrimaryImageUrl(MediaType.AUCTION_ITEM, a.getId(), Duration.ofMinutes(5))
                .orElse(null);

        return PublicMemberProfileResponse.SellAuctionRow.builder()
                .id(a.getId())
                .grade(a.getGrade() != null ? a.getGrade().getGradeCode() : null)
                .title(a.getTitle())
                .currentPrice(a.getCurrentPrice() != null ? a.getCurrentPrice().toPlainString() : null)
                .bidCount(bidCount)
                .remainingSeconds(remaining)
                .primaryImageUrl(imgUrl)
                .wished(Boolean.FALSE) // 공개 API: 비로그인 컨텍스트에서는 찜 여부를 판단할 수 없음
                .tokenId(a.getPhysicalCard() != null && a.getPhysicalCard().getTokenId() != null
                        ? a.getPhysicalCard().getTokenId().longValue()
                        : null)
                .build();
    }

    private PublicMemberProfileResponse.ReviewRow mapReviewToRow(AuctionReview r) {
        return PublicMemberProfileResponse.ReviewRow.builder()
                .id(r.getId())
                .auctionId(r.getAuction().getId())
                .nickname(r.getMember().getNickname())
                .reviewText(r.getReviewText())
                .cardCondition(r.getCardCondition() != null ? r.getCardCondition().name() : null)
                .priceSatisfaction(r.getPriceSatisfaction() != null ? r.getPriceSatisfaction().name() : null)
                .descriptionMatch(r.getDescriptionMatch() != null ? r.getDescriptionMatch().name() : null)
                .star(r.getStar())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toString() : null)
                .build();
    }
}
