package com.bukadong.tcg.api.admin.card.service;

import com.bukadong.tcg.api.admin.card.dto.request.AdminNftCreateRequestDto;
import com.bukadong.tcg.api.admin.card.dto.response.AdminNftCreateResponseDto;
import com.bukadong.tcg.api.card.entity.Card;
import com.bukadong.tcg.api.card.entity.PhysicalCard;
import com.bukadong.tcg.api.card.entity.PhysicalCardStatus;
import com.bukadong.tcg.api.card.event.NftMintEvent;
import com.bukadong.tcg.api.card.repository.CardRepository;
import com.bukadong.tcg.api.card.repository.PhysicalCardRepository;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Hash;

import java.math.BigInteger;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminNftContractServiceImpl implements AdminNftContractService {

    private final PhysicalCardRepository physicalCardRepository;
    private final CardRepository cardRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final String NFT_SECRET_CODE_PREFIX = "TAKO-";

    /**
     * NFT 발행을 '요청'하고 DB에 PENDING 상태로 기록한다.
     * 실제 블록체인 작업은 비동기 이벤트 리스너가 처리
     */
    @Override
    @Transactional
    public AdminNftCreateResponseDto requestNftCreation(AdminNftCreateRequestDto requestDto) {
        /* 1. 요청된 cardId 조회 */
        Card card = cardRepository.findById(requestDto.cardId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CARD_NOT_FOUND));

        /* 2. tokenId, 시크릿 코드 생성 */
        long tokenId = generateUniqueTokenId();
        String secretCode = generateSecretCode();

        /* 3. DB에 PENDING 상태로 저장 */
        PhysicalCard physicalCard = PhysicalCard.builder()
                .card(card)
                .tokenId(BigInteger.valueOf(tokenId))
                .secretHash(Hash.sha3(secretCode))
                .status(PhysicalCardStatus.PENDING)
                .build();
        physicalCardRepository.save(physicalCard);

        /* 4. 비동기 처리를 위한 이벤트 발행 */
        eventPublisher.publishEvent(new NftMintEvent(physicalCard.getId(), tokenId, secretCode));

        return AdminNftCreateResponseDto.builder()
                .physicalCardId(physicalCard.getId())
                .tokenId(tokenId)
                .secret(secretCode)
                .build();
    }

    private long generateUniqueTokenId() {
        return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    }


    private String generateSecretCode() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return NFT_SECRET_CODE_PREFIX + uuid.substring(0, 8).toUpperCase(Locale.ROOT);
    }
}
