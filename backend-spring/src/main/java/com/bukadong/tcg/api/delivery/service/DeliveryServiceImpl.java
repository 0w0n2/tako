package com.bukadong.tcg.api.delivery.service;

import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.auction.entity.AuctionCloseReason;
import com.bukadong.tcg.api.auction.repository.AuctionRepository;
import com.bukadong.tcg.api.delivery.entity.Address;
import com.bukadong.tcg.api.delivery.entity.Delivery;
import com.bukadong.tcg.api.delivery.entity.DeliveryStatus;
import com.bukadong.tcg.api.delivery.repository.AddressRepository;
import com.bukadong.tcg.api.delivery.repository.DeliveryRepository;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryServiceImpl implements DeliveryService {

    private final AuctionRepository auctionRepository;
    private final DeliveryRepository deliveryRepository;
    private final AddressRepository addressRepository;

    private Auction getEndedAuction(long auctionId) {
        Auction a = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.AUCTION_NOT_FOUND));
        if (!a.isEnd() || (a.getCloseReason() != AuctionCloseReason.SOLD
                && a.getCloseReason() != AuctionCloseReason.BUY_NOW)) {
            throw new BaseException(BaseResponseStatus.AUCTION_NOT_ENDED);
        }
        return a;
    }

    @Override
    public Delivery getByAuction(Member requester, long auctionId) {
        Auction a = getEndedAuction(auctionId);
        if (!a.getMember().getId().equals(requester.getId())
                && (a.getWinnerMemberId() == null || !a.getWinnerMemberId().equals(requester.getId()))) {
            throw new BaseException(BaseResponseStatus.ACCESS_DENIED);
        }
        return a.getDelivery();
    }

    @Override
    @Transactional
    public Delivery setSenderAddress(Member seller, long auctionId, long addressId) {
        Auction a = getEndedAuction(auctionId);
        if (!a.getMember().getId().equals(seller.getId())) {
            throw new BaseException(BaseResponseStatus.ACCESS_DENIED);
        }
        Address address = addressRepository.findByIdAndMember(addressId, seller)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));
        Delivery d = a.getDelivery();
        if (d == null) {
            d = Delivery.builder().senderAddress(address).recipientAddress(null).status(DeliveryStatus.WAITING).build();
        } else {
            d = Delivery.builder().id(d.getId()).senderAddress(address).recipientAddress(d.getRecipientAddress())
                    .trackingNumber(d.getTrackingNumber()).status(d.getStatus()).build();
        }
        d = deliveryRepository.save(d);
        if (a.getDelivery() == null || !a.getDelivery().getId().equals(d.getId())) {
            auctionRepository.save(a.toBuilder().delivery(d).build());
        }
        return d;
    }

    @Override
    @Transactional
    public Delivery setRecipientAddress(Member buyer, long auctionId, long addressId) {
        Auction a = getEndedAuction(auctionId);
        if (a.getWinnerMemberId() == null || !a.getWinnerMemberId().equals(buyer.getId())) {
            throw new BaseException(BaseResponseStatus.ACCESS_DENIED);
        }
        Address address = addressRepository.findByIdAndMember(addressId, buyer)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND));
        Delivery d = a.getDelivery();
        if (d == null) {
            d = Delivery.builder().senderAddress(null).recipientAddress(address).status(DeliveryStatus.WAITING).build();
        } else {
            d = Delivery.builder().id(d.getId()).senderAddress(d.getSenderAddress()).recipientAddress(address)
                    .trackingNumber(d.getTrackingNumber()).status(d.getStatus()).build();
        }
        d = deliveryRepository.save(d);
        if (a.getDelivery() == null || !a.getDelivery().getId().equals(d.getId())) {
            auctionRepository.save(a.toBuilder().delivery(d).build());
        }
        return d;
    }

    @Override
    @Transactional
    public Delivery setTrackingNumber(Member seller, long auctionId, String trackingNumber) {
        if (!StringUtils.hasText(trackingNumber)) {
            throw new BaseException(BaseResponseStatus.INVALID_PARAMETER);
        }
        Auction a = getEndedAuction(auctionId);
        if (!a.getMember().getId().equals(seller.getId())) {
            throw new BaseException(BaseResponseStatus.ACCESS_DENIED);
        }
        Delivery d = a.getDelivery();
        if (d == null || d.getSenderAddress() == null || d.getRecipientAddress() == null) {
            throw new BaseException(BaseResponseStatus.BAD_REQUEST);
        }
        Delivery updated = Delivery.builder().id(d.getId()).senderAddress(d.getSenderAddress())
                .recipientAddress(d.getRecipientAddress()).trackingNumber(trackingNumber)
                .status(DeliveryStatus.IN_PROGRESS).build();
        return deliveryRepository.save(updated);
    }

    @Override
    @Transactional
    public void transitionStatuses() {
        // 간단한 상태 전이: IN_PROGRESS가 일정 시간 경과했다고 가정하고 COMPLETED로
        // 세부 규칙은 실제 택배사 연동 시 보강
        var inProgress = deliveryRepository.findByStatus(DeliveryStatus.IN_PROGRESS);
        for (Delivery d : inProgress) {
            Delivery updated = Delivery.builder().id(d.getId()).senderAddress(d.getSenderAddress())
                    .recipientAddress(d.getRecipientAddress()).trackingNumber(d.getTrackingNumber())
                    .status(DeliveryStatus.COMPLETED).build();
            deliveryRepository.save(updated);
        }
    }

    @Override
    @Transactional
    public void confirmByBuyer(Member buyer, long auctionId) {
        Auction a = getEndedAuction(auctionId);
        if (a.getWinnerMemberId() == null || !a.getWinnerMemberId().equals(buyer.getId())) {
            throw new BaseException(BaseResponseStatus.ACCESS_DENIED);
        }
        Delivery d = a.getDelivery();
        if (d == null || d.getStatus() != DeliveryStatus.COMPLETED) {
            throw new BaseException(BaseResponseStatus.BAD_REQUEST);
        }

        // 에스크로 트리거 (stub)
        triggerEscrowPayout();

        Delivery updated = Delivery.builder().id(d.getId()).senderAddress(d.getSenderAddress())
                .recipientAddress(d.getRecipientAddress()).trackingNumber(d.getTrackingNumber())
                .status(DeliveryStatus.CONFIRMED).build();
        deliveryRepository.save(updated);
    }

    private void triggerEscrowPayout() {
        // 스마트 컨트랙트 에스크로 연동은 추후 구현 예정(Stub)
    }
}
