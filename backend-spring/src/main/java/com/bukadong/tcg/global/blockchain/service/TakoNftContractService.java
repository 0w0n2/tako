package com.bukadong.tcg.global.blockchain.service;

import com.bukadong.tcg.api.card.dto.response.NftAuctionHistoryResponseDto;
import com.bukadong.tcg.global.blockchain.contracts.TakoCardNFT;
import com.bukadong.tcg.global.blockchain.util.ContractExceptionHelper;
import com.bukadong.tcg.global.blockchain.util.ContractLoader;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import com.bukadong.tcg.global.properties.blockchain.BlockChainProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Hash;
import org.web3j.protocol.exceptions.TransactionException;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TakoNftContractService {

    private final ContractLoader contractLoader;
    private final ContractExceptionHelper contractExceptionHelper;
    private final BlockChainProperties blockChainProperties;

    private final String LOG_PREFIX = "[BlockChain](TakoNFT)";

    /**
     * NFT를 민팅하고(safeMint) 시크릿을 등록(registerSecret)
     */
    public void mintAndRegisterSecret(BigInteger tokenId, String secret) throws Exception {
        TakoCardNFT contract = contractLoader.loadTakoCardNft();
        String serverWalletAddress = blockChainProperties.sepolia().walletAddress();

        log.debug("{} Attempting safeMint for tokenId: {}", LOG_PREFIX, tokenId);
        contract.safeMint(serverWalletAddress, tokenId).send();
        log.debug("{} safeMint SUCCESS for tokenId: {}", LOG_PREFIX, tokenId);

        log.debug("{} Attempting registerSecret for tokenId: {}", LOG_PREFIX, tokenId);
        byte[] secretHash = Hash.sha3(secret.getBytes());
        contract.registerSecret(tokenId, secretHash).send();
        log.debug("{} registerSecret SUCCESS for tokenId: {}", LOG_PREFIX, tokenId);
    }

    /**
     * 특정 토큰의 경매 기록을 블록체인에서 조회
     */
    public List<NftAuctionHistoryResponseDto> getAuctionHistories(Long tokenId) {
        try {
            TakoCardNFT contract = contractLoader.loadTakoCardNft();
            List<TakoCardNFT.AuctionHistory> histories = contract.getAuctionHistories(BigInteger.valueOf(tokenId)).send();

            return histories.stream()
                    .map(NftAuctionHistoryResponseDto::toDto)
                    .collect(Collectors.toList());
        } catch (TransactionException e) {
            throw contractExceptionHelper.handleTransactionException(e);
        } catch (Exception e) {
            log.error("Failed to get auction histories for tokenId: {}", tokenId, e);
            throw new BaseException(BaseResponseStatus.CONTRACT_EXECUTION_ERROR);
        }
    }
}
