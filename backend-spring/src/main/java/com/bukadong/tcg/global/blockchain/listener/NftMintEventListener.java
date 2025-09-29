package com.bukadong.tcg.global.blockchain.listener;

import com.bukadong.tcg.global.blockchain.event.NftMintEvent;
import com.bukadong.tcg.api.card.service.PhysicalCardService;
import com.bukadong.tcg.global.blockchain.service.TakoNftContractService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * NftMintEvent 를 받아 별도의 스레드에서 (safeMint, registerSecret)을 백그라운드로 실행
 */
@Component
@RequiredArgsConstructor
public class NftMintEventListener {

    private static final Logger log = LoggerFactory.getLogger(NftMintEventListener.class);

    private final PhysicalCardService physicalCardService;
    private final TakoNftContractService takoNftContractService;

    @Async
    @EventListener
    public void handleNftMintEvent(NftMintEvent event) {
        Long physicalCardId = event.physicalCardId();
        log.info("Async event received. Starting blockchain process for PhysicalCard ID: {}", physicalCardId);

        try {
            // safeMint, registerSecret 컨트랙트 실행
            takoNftContractService.mintAndRegisterSecret(event.tokenId(), event.secret());
            // DB 상태 업데이트
            physicalCardService.processMintingSuccess(physicalCardId);
            log.info("Blockchain process SUCCESS for PhysicalCard ID: {}", physicalCardId);
        } catch (Exception e) {
            log.error("Blockchain process FAILED for PhysicalCard ID: {}. Error: {}", physicalCardId, e.getMessage());
            physicalCardService.processMintingFailed(physicalCardId);
        }
    }
}
