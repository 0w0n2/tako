package com.bukadong.tcg.global.blockchain.listener;

import com.bukadong.tcg.api.auction.service.AuctionResultService;
import com.bukadong.tcg.api.auction.service.AuctionSettlementService;
import com.bukadong.tcg.global.blockchain.contracts.AuctionEscrow;
import com.bukadong.tcg.global.blockchain.contracts.AuctionFactory;
import com.bukadong.tcg.global.blockchain.util.ContractLoader;
import com.bukadong.tcg.global.properties.blockchain.BlockChainProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.web3j.abi.EventEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NftAddAuctionHistoryListener {

    private final Web3j web3j;
    private final BlockChainProperties blockChainProperties;
    private final AuctionSettlementService auctionSettlementService;
    private final AuctionResultService auctionResultService;
    private AuctionFactory auctionFactory;
    private final ContractLoader contractLoader;

    private BigInteger lastCheckedBlock = null;


    /**
     * 1분마다 주기적으로 FundsReleased 이벤트를 스캔
     */
    @Scheduled(fixedRate = 60000)
    public void checkForReleaseEvents() {
        try {
            BigInteger latestBlock = web3j.ethBlockNumber().send().getBlockNumber();

            // 리스너 첫 실행 시, 마지막 확인 블록을 현재 블록 -1로 설정
            if (lastCheckedBlock == null) {
                lastCheckedBlock = latestBlock.subtract(BigInteger.ONE);
            }
            
            // 새로운 블록이 생성되었을 경우에만 스캔 실행
            if (latestBlock.compareTo(lastCheckedBlock) > 0) {
                log.debug("Checking for FundsReleased events from block #{} to #{}", lastCheckedBlock.add(BigInteger.ONE), latestBlock);

                // 특정 주소를 지정하지 않고, 모든 컨트랙트를 대상으로 필터링
                EthFilter ethFilter = new EthFilter(
                        DefaultBlockParameter.valueOf(lastCheckedBlock.add(BigInteger.ONE)),
                        DefaultBlockParameterName.LATEST,
                        Collections.emptyList()
                );

                // 감지할 이벤트 토픽 추가
                String eventTopic = EventEncoder.encode(AuctionEscrow.FUNDSRELEASED_EVENT);
                ethFilter.addSingleTopic(eventTopic);

                List<EthLog.LogResult> logResults = web3j.ethGetLogs(ethFilter).send().getLogs();
                if (!logResults.isEmpty()) {
                    log.debug("{} new FundsReleased event(s) found.", logResults.size());
                    for (EthLog.LogResult<?> logResult : logResults) {
                        try {
                            Log logEntry = (Log) logResult.get();
                            AuctionEscrow.FundsReleasedEventResponse response = AuctionEscrow.getFundsReleasedEventFromLog(logEntry);
                            handleFundsReleasedEvent(response, logEntry.getTransactionHash(), logEntry.getAddress());
                        } catch (Exception e) {
                            Log logEntry = (Log) logResult.get();
                            log.error("Failed to process event for contract {}: ", logEntry.getAddress(), e);
                        }
                    }
                }

                lastCheckedBlock = latestBlock;
            }

        } catch (Exception e) {
            log.error("Error while checking for auction events.", e);
        }
    }

    private void handleFundsReleasedEvent(AuctionEscrow.FundsReleasedEventResponse eventResponse, String txHash, String contractAddress) {
        log.debug("FundsReleased event detected! Contract: {}, Seller: {}", contractAddress, eventResponse.seller);
        auctionResultService.recordAuctionHistory(contractAddress);
    }
}
