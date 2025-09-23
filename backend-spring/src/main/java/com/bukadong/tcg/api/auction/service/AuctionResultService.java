package com.bukadong.tcg.api.auction.service;

import com.bukadong.tcg.api.auction.repository.AuctionResultRepository;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionResultService {

    private final AuctionResultRepository auctionResultRepository;

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
}
