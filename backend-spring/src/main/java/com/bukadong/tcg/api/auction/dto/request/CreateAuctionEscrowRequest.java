package com.bukadong.tcg.api.auction.dto.request;

import lombok.Builder;

import java.math.BigInteger;

/**
 * createEscrow 메서드에 필요한 모든 정보를 담는 내부용 DTO
 */
@Builder
public record CreateAuctionEscrowRequest(
        Long auctionId,
        String sellerWalletAddress,
        String buyerWalletAddress,
        BigInteger amountInWei,
        String nftContractAddress,
        Long tokenId
) {
}
