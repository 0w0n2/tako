package com.bukadong.tcg.api.delivery.service;

import com.bukadong.tcg.api.delivery.entity.Delivery;
import com.bukadong.tcg.api.member.entity.Member;

public interface DeliveryService {
    Delivery getByAuction(Member requester, long auctionId);

    Delivery setSenderAddress(Member seller, long auctionId, long addressId);

    Delivery setRecipientAddress(Member buyer, long auctionId, long addressId);

    Delivery setTrackingNumber(Member seller, long auctionId, String trackingNumber);

    void transitionStatuses(); // WAITING -> IN_PROGRESS -> COMPLETED (스케줄러에서 호출)

    void confirmByBuyer(Member buyer, long auctionId); // 구매 확정 + 에스크로 트리거
}
