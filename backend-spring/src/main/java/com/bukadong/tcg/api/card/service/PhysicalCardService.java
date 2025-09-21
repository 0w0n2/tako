package com.bukadong.tcg.api.card.service;

import com.bukadong.tcg.api.card.dto.request.NftClaimStatusResponseDto;
import com.bukadong.tcg.api.card.entity.PhysicalCard;
import com.bukadong.tcg.api.card.entity.PhysicalCardStatus;
import com.bukadong.tcg.api.card.repository.PhysicalCardRepository;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class PhysicalCardService {
    private final PhysicalCardRepository physicalCardRepository;

    @Transactional(readOnly = true)
    public NftClaimStatusResponseDto checkClaimStatus(Long tokenId) {
        PhysicalCard physicalCard = physicalCardRepository.findByTokenId(BigInteger.valueOf(tokenId))
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PHYSICAL_CARD_NOT_FOUND));

        return NftClaimStatusResponseDto.builder()
                .isClaimable(physicalCard.getStatus() == PhysicalCardStatus.MINTED)
                .message(physicalCard.getStatus().getMessage())
                .build();
    }
}
