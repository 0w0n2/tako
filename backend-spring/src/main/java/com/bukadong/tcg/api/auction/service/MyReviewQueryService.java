package com.bukadong.tcg.api.auction.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bukadong.tcg.api.auction.dto.request.MyReviewRole;
import com.bukadong.tcg.api.auction.dto.response.MyReviewItemResponse;
import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.auction.entity.AuctionReview;
import com.bukadong.tcg.api.auction.repository.AuctionRepository;
import com.bukadong.tcg.api.media.entity.MediaType;
import com.bukadong.tcg.api.media.service.MediaUrlService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyReviewQueryService {

    private final AuctionRepository auctionRepository;
    private final MediaUrlService mediaUrlService;

    @Transactional(readOnly = true)
    public List<MyReviewItemResponse> listMe(Long meId, MyReviewRole role, boolean done) {
        if (role == MyReviewRole.BUYER) {
            return done ? listBuyerDone(meId) : listBuyerAwaiting(meId);
        } else {
            return done ? listSellerDone(meId) : listSellerAwaiting(meId);
        }
    }

    private List<MyReviewItemResponse> listBuyerAwaiting(Long buyerId) {
        List<Auction> auctions = auctionRepository.findBuyerAwaitingReview(buyerId);
        List<MyReviewItemResponse> items = new ArrayList<>(auctions.size());
        for (Auction a : auctions) {
            String primaryImageUrl = mediaUrlService
                    .getPresignedImageUrls(MediaType.AUCTION_ITEM, a.getId(), Duration.ofMinutes(5)).stream()
                    .findFirst().orElse(null);
            var delivery = a.getDelivery();
            var deliverySummary = new MyReviewItemResponse.DeliverySummary(
                    delivery == null ? null : delivery.getStatus().name(),
                    delivery != null && delivery.getTrackingNumber() != null && !delivery.getTrackingNumber().isBlank(),
                    delivery != null && delivery.getRecipientAddress() != null,
                    delivery != null && delivery.getSenderAddress() != null);
            items.add(new MyReviewItemResponse(a.getId(), a.getCode(), a.getTitle(), a.getStartDatetime(),
                    a.getEndDatetime(), a.isEnd(), a.getCloseReason() == null ? null : a.getCloseReason().name(),
                    a.getCurrentPrice(), primaryImageUrl, deliverySummary, null));
        }
        return items;
    }

    private List<MyReviewItemResponse> listBuyerDone(Long buyerId) {
        List<Object[]> rows = auctionRepository.findBuyerDoneWithReview(buyerId);
        List<MyReviewItemResponse> items = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            Auction a = (Auction) r[0];
            AuctionReview review = (AuctionReview) r[1];
            String primaryImageUrl = mediaUrlService
                    .getPresignedImageUrls(MediaType.AUCTION_ITEM, a.getId(), Duration.ofMinutes(5)).stream()
                    .findFirst().orElse(null);
            var delivery = a.getDelivery();
            var deliverySummary = new MyReviewItemResponse.DeliverySummary(
                    delivery == null ? null : delivery.getStatus().name(),
                    delivery != null && delivery.getTrackingNumber() != null && !delivery.getTrackingNumber().isBlank(),
                    delivery != null && delivery.getRecipientAddress() != null,
                    delivery != null && delivery.getSenderAddress() != null);
            var reviewSummary = new MyReviewItemResponse.ReviewSummary(review.getId(),
                    maskNickname(review.getMember().getNickname()), review.getReviewText(), review.getCardCondition(),
                    review.getPriceSatisfaction(), review.getDescriptionMatch(), review.getStar(),
                    review.getCreatedAt());
            items.add(new MyReviewItemResponse(a.getId(), a.getCode(), a.getTitle(), a.getStartDatetime(),
                    a.getEndDatetime(), a.isEnd(), a.getCloseReason() == null ? null : a.getCloseReason().name(),
                    a.getCurrentPrice(), primaryImageUrl, deliverySummary, reviewSummary));
        }
        return items;
    }

    private List<MyReviewItemResponse> listSellerAwaiting(Long sellerId) {
        List<Auction> auctions = auctionRepository.findSellerAwaitingReview(sellerId);
        List<MyReviewItemResponse> items = new ArrayList<>(auctions.size());
        for (Auction a : auctions) {
            String primaryImageUrl = mediaUrlService
                    .getPresignedImageUrls(MediaType.AUCTION_ITEM, a.getId(), Duration.ofMinutes(5)).stream()
                    .findFirst().orElse(null);
            var delivery = a.getDelivery();
            var deliverySummary = new MyReviewItemResponse.DeliverySummary(
                    delivery == null ? null : delivery.getStatus().name(),
                    delivery != null && delivery.getTrackingNumber() != null && !delivery.getTrackingNumber().isBlank(),
                    delivery != null && delivery.getRecipientAddress() != null,
                    delivery != null && delivery.getSenderAddress() != null);
            items.add(new MyReviewItemResponse(a.getId(), a.getCode(), a.getTitle(), a.getStartDatetime(),
                    a.getEndDatetime(), a.isEnd(), a.getCloseReason() == null ? null : a.getCloseReason().name(),
                    a.getCurrentPrice(), primaryImageUrl, deliverySummary, null));
        }
        return items;
    }

    private List<MyReviewItemResponse> listSellerDone(Long sellerId) {
        List<Object[]> rows = auctionRepository.findSellerDoneWithReview(sellerId);
        List<MyReviewItemResponse> items = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            Auction a = (Auction) r[0];
            AuctionReview review = (AuctionReview) r[1];
            String primaryImageUrl = mediaUrlService
                    .getPresignedImageUrls(MediaType.AUCTION_ITEM, a.getId(), Duration.ofMinutes(5)).stream()
                    .findFirst().orElse(null);
            var delivery = a.getDelivery();
            var deliverySummary = new MyReviewItemResponse.DeliverySummary(
                    delivery == null ? null : delivery.getStatus().name(),
                    delivery != null && delivery.getTrackingNumber() != null && !delivery.getTrackingNumber().isBlank(),
                    delivery != null && delivery.getRecipientAddress() != null,
                    delivery != null && delivery.getSenderAddress() != null);
            var reviewSummary = new MyReviewItemResponse.ReviewSummary(review.getId(),
                    maskNickname(review.getMember().getNickname()), review.getReviewText(), review.getCardCondition(),
                    review.getPriceSatisfaction(), review.getDescriptionMatch(), review.getStar(),
                    review.getCreatedAt());
            items.add(new MyReviewItemResponse(a.getId(), a.getCode(), a.getTitle(), a.getStartDatetime(),
                    a.getEndDatetime(), a.isEnd(), a.getCloseReason() == null ? null : a.getCloseReason().name(),
                    a.getCurrentPrice(), primaryImageUrl, deliverySummary, reviewSummary));
        }
        return items;
    }

    private String maskNickname(String nickname) {
        if (nickname == null || nickname.isBlank())
            return "";
        if (nickname.length() <= 2) {
            return nickname.charAt(0) + "*";
        }
        return nickname.substring(0, 3) + "***";
    }
}
