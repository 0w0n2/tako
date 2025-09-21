package com.bukadong.tcg.global.blockchain.service;

import com.bukadong.tcg.api.card.dto.response.NftAuctionHistoryResponseDto;
import com.bukadong.tcg.global.blockchain.contracts.TakoCardNFT;
import com.bukadong.tcg.global.blockchain.util.ContractExceptionHelper;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import com.bukadong.tcg.global.properties.blockchain.BlockChainProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TakoNftContractService {

    private final Web3j web3j;
    private final BlockChainProperties blockChainProperties;
    private final ContractExceptionHelper contractExceptionHelper;

    public List<NftAuctionHistoryResponseDto> getAuctionHistories(Long tokenId) {
        try {
            TakoCardNFT contract = loadContract();
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
