package com.bukadong.tcg.global.blockchain.service;

import com.bukadong.tcg.api.auction.dto.request.CreateAuctionEscrowRequest;
import com.bukadong.tcg.api.auction.dto.response.CreateAuctionEscrowResponse;
import com.bukadong.tcg.global.blockchain.contracts.AuctionFactory;
import com.bukadong.tcg.global.blockchain.util.ContractExceptionHelper;
import com.bukadong.tcg.global.blockchain.util.ContractLoader;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import com.bukadong.tcg.global.properties.blockchain.BlockChainProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionContractService {

    private final ContractExceptionHelper contractExceptionHelper;
    private final ContractLoader contractLoader;

    /**
     * AuctionFactory를 통해 새로운 AuctionEscrow 컨트랙트 생성
     */
    public Optional<String> createEscrow(CreateAuctionEscrowRequest requestDto) {
        try {
            log.info("블록체인 작업 시작: AuctionEscrow 생성 (Auction ID: {})", requestDto.auctionId());
            AuctionFactory auctionFactory = contractLoader.loadAuctionFactory();

            TransactionReceipt receipt = auctionFactory.createEscrow(
                    requestDto.sellerWalletAddress(),
                    requestDto.buyerWalletAddress(),
                    requestDto.amountInWei(),
                    requestDto.nftContractAddress(),
                    BigInteger.valueOf(requestDto.tokenId())
            ).send();

            /* 새 AuctionEscrow 컨트랙트 주소 파싱 */
            List<AuctionFactory.EscrowCreatedEventResponse> events = AuctionFactory.getEscrowCreatedEvents(receipt);

            if (events.isEmpty()) {
                log.error("EscrowCreated 이벤트를 트랜잭션 영수증에서 찾을 수 없습니다. TxHash: {}", receipt.getTransactionHash());
                throw new BaseException(BaseResponseStatus.AUCTION_ESCROW_CONTRACT_NOT_FOUND);
            }

            String newEscrowAddress = events.get(0).newEscrowAddress;
            log.info("블록체인 작업 성공: AuctionEscrow 생성 완료. Address: {}", newEscrowAddress);
            return Optional.of(newEscrowAddress);
        } catch (TransactionException e) {
            String context = "Auction ID: " + requestDto.auctionId();
            contractExceptionHelper.logTransactionException(e, context);
            return Optional.empty();
        } catch (Exception e) {
            log.error("AuctionEscrow 생성 중 예외 발생: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}
