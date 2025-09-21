package com.bukadong.tcg.api.admin.card.service;

import com.bukadong.tcg.api.admin.card.dto.response.PhysicalCardStatusResponseDto;
import com.bukadong.tcg.api.card.entity.PhysicalCard;
import com.bukadong.tcg.api.card.repository.PhysicalCardRepository;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminPhysicalCardService {

    private final PhysicalCardRepository physicalCardRepository;

    /**
     * 특정 실물 카드의 NFT 발행 상태를 조회
     */
    @Transactional(readOnly = true)
    public PhysicalCardStatusResponseDto getPhysicalCardStatus(Long physicalCardId) {
        PhysicalCard physicalCard = physicalCardRepository.findById(physicalCardId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PHYSICAL_CARD_NOT_FOUND));
        return PhysicalCardStatusResponseDto.toDto(physicalCard);
    }

}
