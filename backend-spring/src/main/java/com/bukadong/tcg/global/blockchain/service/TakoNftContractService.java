package com.bukadong.tcg.global.blockchain.service;

import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.auction.entity.AuctionResult;
import com.bukadong.tcg.api.card.dto.response.NftAuctionHistoryResponseDto;
import com.bukadong.tcg.api.card.entity.PhysicalCard;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.global.blockchain.contracts.AuctionEscrow;
import com.bukadong.tcg.global.blockchain.contracts.TakoCardNFT;
import com.bukadong.tcg.global.blockchain.util.ContractExceptionHelper;
import com.bukadong.tcg.global.blockchain.util.ContractLoader;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import com.bukadong.tcg.global.properties.blockchain.BlockChainProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Hash;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
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

    /**
     * NFT를 민팅하고(safeMint) 시크릿을 등록(registerSecret)
     */
    public void mintAndRegisterSecret(BigInteger tokenId, String secret) throws Exception {
        TakoCardNFT contract = contractLoader.loadTakoCardNft();
        String serverWalletAddress = blockChainProperties.sepolia().walletAddress();

        log.debug("Attempting safeMint for tokenId: {}", tokenId);
        contract.safeMint(serverWalletAddress, tokenId).send();
        log.debug("safeMint SUCCESS for tokenId: {}", tokenId);

        log.debug("Attempting registerSecret for tokenId: {}", tokenId);
        byte[] secretHash = Hash.sha3(secret.getBytes());
        contract.registerSecret(tokenId, secretHash).send();
        log.debug("registerSecret SUCCESS for tokenId: {}", tokenId);
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

    /**
     * 특정 토큰의 소유주 지갑 주소를 블록체인에서 조회
     */
    public String getOwnerAddress(Long tokenId) {
        try {
            TakoCardNFT contract = contractLoader.loadTakoCardNft();
            return contract.ownerOf(BigInteger.valueOf(tokenId)).send();
        } catch (TransactionException e) {
            throw contractExceptionHelper.handleTransactionException(e);
        } catch (Exception e) {
            log.error("Failed to get owner address for tokenId: {}", tokenId, e);
            throw new BaseException(BaseResponseStatus.CONTRACT_EXECUTION_ERROR);
        }
    }

    /**
     * 경매 내역을 NFT 카드에 등록
     */
    public TransactionReceipt addAuctionHistory(BigInteger tokenId, String sellerAddress, String buyerAddress, BigDecimal amount, BigInteger gradeId) {
        try {
            /* NFT 에 등록 */
            TakoCardNFT nftContract = contractLoader.loadTakoCardNft();
            BigInteger amountInWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();

            return nftContract.addAuctionHistory(
                    tokenId,
                    sellerAddress,
                    buyerAddress,
                    amountInWei,
                    gradeId
            ).send();
        } catch (TransactionException e) {
            throw contractExceptionHelper.handleTransactionException(e);
        } catch (Exception e) {
            log.error("Failed to add auction history for tokenId: {}", tokenId, e);
            throw new BaseException(BaseResponseStatus.CONTRACT_EXECUTION_ERROR);
        }
    }
}
