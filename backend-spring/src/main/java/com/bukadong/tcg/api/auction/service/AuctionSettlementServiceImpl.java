package com.bukadong.tcg.api.auction.service;

import java.math.BigDecimal;
import java.util.Optional;

import com.bukadong.tcg.api.auction.dto.request.CreateAuctionEscrowRequest;
import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.auction.repository.AuctionRepository;
import com.bukadong.tcg.api.auction.repository.AuctionResultRepository;
import com.bukadong.tcg.api.card.entity.PhysicalCard;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.api.member.repository.MemberRepository;
import com.bukadong.tcg.global.blockchain.service.AuctionContractService;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import com.bukadong.tcg.global.properties.blockchain.BlockChainProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.web3j.utils.Convert;

/**
 * 경매 정산 서비스 기본 구현
 * <p>
 * 실서비스에선 MQ/배치로 연결하면 되고, 현재는 로그 적재로 대체한다.
 * </P>
 *
 * @PARAM auctionId 경매 ID
 * @PARAM winnerMemberId 낙찰자 회원 ID
 * @PARAM amount 낙찰 금액
 * @RETURN 없음
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionSettlementServiceImpl implements AuctionSettlementService {
    private final AuctionRepository auctionRepository;
    private final MemberRepository memberRepository;
    private final BlockChainProperties blockChainProperties;
    private final AuctionContractService auctionContractService;
    private final AuctionResultService auctionResultService;

    @Override
    @Transactional
    public void enqueue(Long auctionId, Long winnerMemberId, BigDecimal amount) {
        // TODO: MQ/이체/정산 배치에 전달하도록 확장
        log.info("[Settlement] enqueue auctionId={}, winnerMemberId={}, amount={}", auctionId, winnerMemberId, amount);
    }

    /**
     * 경매 종료 후 에스크로 생성 및 결과 저장 처리
     */
    @Override
    public void createEscrowForAuction(Long auctionId, Long winnerMemberId, BigDecimal amount) {
        /* 경매 정보 조회 */
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalStateException("Auction is not found while createEscrowForAuction"));
        Member seller = auction.getMember();
        Member buyer = memberRepository.findById(winnerMemberId)
                .orElseThrow(() -> new IllegalStateException("Buyer is not found while createEscrowForAuction"));
    
        /* 지갑 정보 유효성 검사 */
        if (!StringUtils.hasText(seller.getWalletAddress()) || !StringUtils.hasText(buyer.getWalletAddress())) {
            log.error("판매자 또는 구매자의 지갑 주소가 등록되지 않아 에스크로 생성을 중단합니다. Auction ID: {}", auctionId);
            // TODO: 관리자에게 알림을 보내는 등의 후속 처리 필요
            return;
        }

        PhysicalCard physicalCard = auction.getPhysicalCard();
        String nftContractAddress;
        long tokenID;

        if (physicalCard != null && physicalCard.getTokenId() != null) {
            /* NFT 경매인 경우 : 실제 NFT 정보 설정 */
            nftContractAddress = blockChainProperties.contractAddress().takoCardNft();
            tokenID = physicalCard.getTokenId().longValue();
        } else {
            /* 일반 경매인 경우 : Address(0), tokenId(0)으로 설정 */
            nftContractAddress = "0x0000000000000000000000000000000000000000";
            tokenID = 0L;
        }

        /* 블록체인 작업을 위한 데이터 준비 */
        CreateAuctionEscrowRequest escrowRequest = CreateAuctionEscrowRequest.builder()
                .auctionId(auctionId)
                .sellerWalletAddress(seller.getWalletAddress())
                .buyerWalletAddress(buyer.getWalletAddress())
                .amountInWei(Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger())
                .nftContractAddress(nftContractAddress)
                .tokenId(tokenID)
                .build();

        /* 블록체인 컨트랙트 작업 수행 */
        Optional<String> escrowAddressOpt = auctionContractService.createEscrow(escrowRequest);

        /* DB 업데이트 */
        escrowAddressOpt.ifPresent(escrowAddress -> {
            auctionResultService.saveEscrowResult(auctionId, escrowAddress);
        });
    }
}
