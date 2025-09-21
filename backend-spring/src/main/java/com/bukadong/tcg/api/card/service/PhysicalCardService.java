package com.bukadong.tcg.api.card.service;

import com.bukadong.tcg.api.admin.card.dto.response.NftCreateResponseDto;
import com.bukadong.tcg.api.admin.card.dto.response.PhysicalCardStatusResponseDto;
import com.bukadong.tcg.api.admin.card.event.NftMintEvent;
import com.bukadong.tcg.api.card.dto.request.NftClaimStatusResponseDto;
import com.bukadong.tcg.api.card.entity.Card;
import com.bukadong.tcg.api.card.entity.PhysicalCard;
import com.bukadong.tcg.api.card.entity.PhysicalCardStatus;
import com.bukadong.tcg.api.card.repository.CardRepository;
import com.bukadong.tcg.api.card.repository.PhysicalCardRepository;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Hash;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.UUID;

/**
 * PhysicalCard의 생성, 상태 변경, 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhysicalCardService {

    private final PhysicalCardRepository physicalCardRepository;
    private final CardRepository cardRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final String NFT_SECRET_CODE_PREFIX = "TAKO-";
    private static final SecureRandom secureRandom = new SecureRandom();

    // --- 1. NFT 카드 생성 ---
    @Transactional
    public NftCreateResponseDto requestNftCreation(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CARD_NOT_FOUND));

        /* tokenId, Secret 생성 */
        BigInteger tokenId = generateUniqueTokenId();
        String secretCode = generateSecretCode();

        /* DB에 PENDING 상태로 저장 */
        PhysicalCard physicalCard = PhysicalCard.builder()
                .card(card)
                .tokenId(tokenId)
                .secretHash(Hash.sha3(secretCode))
                .status(PhysicalCardStatus.PENDING)
                .build();
        physicalCardRepository.save(physicalCard);

        /* 비동기 처리를 위한 이벤트 발행 */
        eventPublisher.publishEvent(new NftMintEvent(physicalCard.getId(), tokenId, secretCode));

        return NftCreateResponseDto.toDto(physicalCard, tokenId, secretCode);
    }

    // --- 2. 상태 변경 (이벤트 리스너에 의해 호출) ---
    @Transactional
    public void processMintingSuccess(Long physicalCardId) {
        findById(physicalCardId).markAsMinted();
    }

    @Transactional
    public void processMintingFailed(Long physicalCardId) {
        findById(physicalCardId).markAsFailed();
    }

    @Transactional
    public void processClaim(Long physicalCardId, String ownerWalletAddress, Member newOwner) {
        findById(physicalCardId).markAsClaimed(ownerWalletAddress, newOwner);
    }

    // --- 3. 상태 조회 ---
    @Transactional(readOnly = true)
    public PhysicalCardStatusResponseDto getCardStatusForAdmin(Long physicalCardId) {
        return PhysicalCardStatusResponseDto.toDto(findById(physicalCardId));
    }

    @Transactional(readOnly = true)
    public NftClaimStatusResponseDto getCardStatusForUser(Long tokenId) {
        PhysicalCard physicalCard = findByTokenId(BigInteger.valueOf(tokenId));
        return NftClaimStatusResponseDto.toDto(physicalCard);
    }

    // --- 4. Private Helper 메소드 ---
    private PhysicalCard findById(Long id) {
        return physicalCardRepository.findById(id)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PHYSICAL_CARD_NOT_FOUND));
    }

    private PhysicalCard findByTokenId(BigInteger tokenId) {
        return physicalCardRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PHYSICAL_CARD_NOT_FOUND));
    }

    // 100,000,000 ~ 999,999,999 범위의 9자리 난수 생성
    private BigInteger generateUniqueTokenId() {
        for (int i = 0; i < 10; i++) {
            long randomNineDigitNumber = 100_000_000L + secureRandom.nextLong(900_000_000L);
            BigInteger tokenId = BigInteger.valueOf(randomNineDigitNumber);
            if (!physicalCardRepository.existsByTokenId(tokenId)) {
                return tokenId;
            }
        }
        throw new BaseException(BaseResponseStatus.NFT_ID_GENERATE_FAIL);
    }

    private String generateSecretCode() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return NFT_SECRET_CODE_PREFIX + uuid.substring(0, 8).toUpperCase(Locale.ROOT);
    }
}
