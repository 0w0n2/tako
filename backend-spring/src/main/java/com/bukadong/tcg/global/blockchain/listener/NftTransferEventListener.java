package com.bukadong.tcg.global.blockchain.listener;

import com.bukadong.tcg.api.card.repository.PhysicalCardRepository;
import com.bukadong.tcg.api.card.service.PhysicalCardService;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.repository.MemberRepository;
import com.bukadong.tcg.global.blockchain.contracts.TakoCardNFT;
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

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 * NFT 카드의 소유권이 이전되는 것을 감지하고 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NftTransferEventListener {

    private final Web3j web3j;
    private final BlockChainProperties blockChainProperties;
    private final PhysicalCardRepository physicalCardRepository;
    private final MemberRepository memberRepository;
    private final PhysicalCardService physicalCardService;

    private BigInteger lastCheckedBlock = null;

    /* 15초마다 새로운 블록의 Transfer 이벤트를 확인 */
    @Scheduled(fixedRate = 15000)
    public void checkForTransferEvents() {
        try {
            String contractAddress = blockChainProperties.contractAddress().takoCardNft();
            BigInteger latestBlock = web3j.ethBlockNumber().send().getBlockNumber();

            if (lastCheckedBlock == null) {
                lastCheckedBlock = latestBlock.subtract(BigInteger.ONE);
            }

            if (latestBlock.compareTo(lastCheckedBlock) > 0) {
                log.debug("Checking for Transfer events from block #{} to #{}", lastCheckedBlock.add(BigInteger.ONE), latestBlock);
                EthFilter filter = new EthFilter(
                        DefaultBlockParameter.valueOf(lastCheckedBlock.add(BigInteger.ONE)),
                        DefaultBlockParameterName.LATEST,    // 항상 최신 블록까지 조회
                        contractAddress
                );
                String transferEventTopic = EventEncoder.encode(TakoCardNFT.TRANSFER_EVENT);
                filter.addSingleTopic(transferEventTopic);

                List<EthLog.LogResult> logResults = web3j.ethGetLogs(filter).send().getLogs();

                if (!logResults.isEmpty()) {
                    log.debug("{} new Transfer event(s) found.", logResults.size());
                    for (EthLog.LogResult<?> logResult : logResults) {
                        Log logEntry = (Log) logResult.get();
                        TakoCardNFT.TransferEventResponse response = TakoCardNFT.getTransferEventFromLog(logEntry);
                        handleClaimEvent(response.from, response.to, response.tokenId);
                    }
                }
                lastCheckedBlock = latestBlock;
            }
        } catch (IOException e) {
            log.error("Failed to check for new blocks or process events.", e);
        }
    }

    private void handleClaimEvent(String from, String to, BigInteger tokenId) {
        String serverAddress = blockChainProperties.sepolia().walletAddress();

        if (from.equalsIgnoreCase(serverAddress) && !to.equalsIgnoreCase(serverAddress)) {
            log.debug("Claim event detected! TokenId: {}, New Owner Address: {}", tokenId, to);

            physicalCardRepository.findByTokenId(tokenId).ifPresent(physicalCard -> {
                Optional<Member> ownerOpt = memberRepository.findByWalletAddress(to);
                physicalCardService.processClaim(physicalCard.getId(), to, ownerOpt.orElse(null));

                if (ownerOpt.isPresent()) {
                    log.debug("PhysicalCard (ID: {}) status updated to CLAIMED for Member (ID: {})",
                            physicalCard.getId(), ownerOpt.get().getId());
                } else {
                    log.debug("Wallet address {} is not a registered member. Linking card to wallet address directly.", to);
                }
            });
        }
    }
}
