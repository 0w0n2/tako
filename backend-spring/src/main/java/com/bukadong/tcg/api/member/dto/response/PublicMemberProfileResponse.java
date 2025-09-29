package com.bukadong.tcg.api.member.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PublicMemberProfileResponse {
    Long memberId;
    String email;
    String nickname;
    String introduction;
    String profileImageUrl;
    String backgroundImageUrl;
    List<SellAuctionRow> sellAuctions;
    List<ReviewRow> reviews;

    @Value
    @Builder
    public static class SellAuctionRow {
        Long id;
        String grade;
        String title;
        String currentPrice;
        Long bidCount;
        Long remainingSeconds;
        String primaryImageUrl;
        Boolean wished;
        Long tokenId;
    }

    @Value
    @Builder
    public static class ReviewRow {
        Long id;
        Long auctionId;
        String nickname;
        String reviewText;
        String cardCondition;
        String priceSatisfaction;
        String descriptionMatch;
        Integer star;
        String createdAt;
    }
}
