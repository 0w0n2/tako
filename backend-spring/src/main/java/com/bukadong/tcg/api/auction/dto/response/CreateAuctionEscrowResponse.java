package com.bukadong.tcg.api.auction.dto.response;

import lombok.Builder;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

/**
 * createEscrow 메서드의 성공 결과를 담는 내부용 DTO
 */
@Builder
public record CreateAuctionEscrowResponse(
        String escrowContractAddress,
        String transactionHash
) {
    public static CreateAuctionEscrowResponse toDto(String newEscrowAddress, TransactionReceipt receipt) {
        return CreateAuctionEscrowResponse.builder()
                .escrowContractAddress(newEscrowAddress)
                .transactionHash(receipt.getTransactionHash())
                .build();
    }
}
