package com.bukadong.tcg.api.auction.service;

import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.auction.entity.AuctionResult;
import com.bukadong.tcg.api.auction.repository.AuctionResultRepository;
import com.bukadong.tcg.api.delivery.service.DeliveryService;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import com.bukadong.tcg.api.card.entity.PhysicalCard;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.global.blockchain.service.TakoNftContractService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class AuctionResultService {

    private static final Logger log = LoggerFactory.getLogger(AuctionResultService.class);

    private final AuctionResultRepository auctionResultRepository;
    private final TakoNftContractService takoNftContractService;
    private final DeliveryService deliveryService;

    /* 에스크로 생성 성공 결과를 DB에 저장 */
    @Transactional
    public void saveEscrowResult(Long auctionId, String escrowAddress) {
        auctionResultRepository.findByAuctionId(auctionId).ifPresent(result -> {
            result.updateSettleTxHash(escrowAddress);
        });
        log.debug("[Settlement] Auction ID: {} -> Escrow contract address saved: {}", auctionId, escrowAddress);
    }

    @Transactional(readOnly = true)
    public String getEscrowAddress(Long auctionId) {
        String contractAddress = auctionResultRepository.findSettleTxHash(auctionId);
        if (!StringUtils.hasText(contractAddress)) {
            throw new BaseException(BaseResponseStatus.AUCTION_ESCROW_CONTRACT_NOT_FOUND);
        }
        return contractAddress;
    }

    /**
     * 블록체인 이벤트로부터 경매 완료 기록을 처리하고, NFT 컨트랙트에 기록
     */
    @Transactional
    public void recordAuctionHistory(String escrowContractAddress) {
        AuctionResult auctionResult = auctionResultRepository.findBySettleTxHash(escrowContractAddress)
                .orElse(null);

        if (auctionResult == null) {
            log.warn("An event was detected from an unknown Escrow contract: {}", escrowContractAddress);
            return;
        }

        if (auctionResult.isSettledFlag()) {
            log.warn("AuctionResult #{} has already been updated about settled Flag.", auctionResult.getId());
            return;
        }

        Auction auction = auctionResult.getAuction();
        PhysicalCard physicalCard = auction.getPhysicalCard();
        Member seller = auction.getMember();
        Member buyer = auctionResult.getAuctionBid().getMember();

        if (physicalCard != null && physicalCard.getTokenId() != null) {
            TransactionReceipt receipt = takoNftContractService.addAuctionHistory(
                    physicalCard.getTokenId(),
                    seller.getWalletAddress(),
                    buyer.getWalletAddress(),
                    auctionResult.getAuctionBid().getAmount(),
                    BigInteger.valueOf(auction.getGrade().getId()));
        }

        deliveryService.confirmByBuyer(buyer, auction.getId());
        auctionResult.updateSettleFlag(true);
    }
}
