package com.bukadong.tcg.api.auction.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;

import com.bukadong.tcg.api.auction.dto.response.AuctionReviewResponse;
import com.bukadong.tcg.api.auction.repository.AuctionReviewRepository;

@Service
@RequiredArgsConstructor
public class ReviewQueryService {

    private final AuctionReviewRepository auctionReviewRepository;

    /**
     * 회원이 받은 후기 조회
     * <P>
     * 작성자 닉네임은 일부만 노출한다.
     * </P>
     * 
     * @PARAM memberId 회원 ID
     * @RETURN 후기 응답 리스트
     */
    @Transactional(readOnly = true)
    public List<AuctionReviewResponse> getReviewsByMember(Long memberId) {
        return auctionReviewRepository.findByAuctionMemberId(memberId).stream()
                .map(review -> AuctionReviewResponse.builder().id(review.getId()).auctionId(review.getAuction().getId())
                        .nickname(maskNickname(review.getMember().getNickname())).reviewText(review.getReviewText())
                        .cardCondition(review.getCardCondition()).priceSatisfaction(review.getPriceSatisfaction())
                        .descriptionMatch(review.getDescriptionMatch()).star(review.getStar())
                        .createdAt(review.getCreatedAt()).build())
                .toList();
    }

    /** 닉네임 일부만 마스킹 처리 */
    private String maskNickname(String nickname) {
        if (nickname.length() <= 2) {
            return nickname.charAt(0) + "*";
        }
        return nickname.substring(0, 3) + "***";
    }
}
