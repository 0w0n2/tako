package com.bukadong.tcg.api.card.listener;

import com.bukadong.tcg.api.admin.card.service.PhysicalCardStatusService;
import com.bukadong.tcg.api.card.event.NftMintEvent;
import com.bukadong.tcg.global.blockchain.contracts.TakoCardNFT;
import com.bukadong.tcg.global.blockchain.util.ContractExceptionHelper;
import com.bukadong.tcg.global.properties.blockchain.BlockChainProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;

/**
 * NftMintEvent 를 받아 별도의 스레드에서 (safeMint, registerSecret)을 백그라운드로 실행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NftMintEventListener {

    private final Web3j web3j;
    private final BlockChainProperties blockChainProperties;
    private final PhysicalCardStatusService physicalCardStatusService;
    private final ContractExceptionHelper contractExceptionHelper;

    @Async
    @EventListener
    public void handleNftMintEvent(NftMintEvent event) {
        log.info("Async event received. Starting blockchain process for PhysicalCard ID: {}", event.physicalCardId());

        try {
            // safeMint, registerSecret 컨트랙트 실행
            mintNft(event.tokenId());
            registerSecret(event.tokenId(), event.secret());

            // DB 상태 업데이트
            physicalCardStatusService.updateStatusToMinted(event.physicalCardId(), event.secret());
            log.info("Blockchain process SUCCESS for PhysicalCard ID: {}", event.physicalCardId());

        } catch (TransactionException e) {
            String failureMessage = contractExceptionHelper.handleTransactionException(e).getMessage();
            log.error(
                    "Blockchain process FAILED for PhysicalCard ID: {}. Decoded Reason: {}",
                    event.physicalCardId(),
                    failureMessage
            );
            physicalCardStatusService.updateStatusToFailed(event.physicalCardId());
        }
        catch (Exception e) {
            log.error("Blockchain process FAILED for PhysicalCard ID: {}. Error: {}", event.physicalCardId(), e.getMessage());
            physicalCardStatusService.updateStatusToFailed(event.physicalCardId());
        }
    }


    private void mintNft(long tokenId) throws Exception {
        TakoCardNFT contract = loadContract();
        String serverWalletAddress = blockChainProperties.sepolia().walletAddress();
        contract.safeMint(serverWalletAddress, BigInteger.valueOf(tokenId)).send();
    }

    private void registerSecret(long tokenId, String secretCode) throws Exception {
        TakoCardNFT contract = loadContract();
        byte[] secretHash = Hash.sha3(secretCode.getBytes());
        contract.registerSecret(BigInteger.valueOf(tokenId), secretHash).send();
    }

    private TakoCardNFT loadContract() {
        String privateKey = blockChainProperties.sepolia().privateKey();
        Credentials credentials = Credentials.create(privateKey);   // 관리자 계정

        String contractAddress = blockChainProperties.contractAddress().takoCardNft();

        return TakoCardNFT.load(
                contractAddress,
                web3j,
                credentials,
                new DefaultGasProvider()
        );
    }
}
