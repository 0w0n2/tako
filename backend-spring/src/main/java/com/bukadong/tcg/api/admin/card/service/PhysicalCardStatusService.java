package com.bukadong.tcg.api.admin.card.service;

import com.bukadong.tcg.api.card.entity.PhysicalCard;
import com.bukadong.tcg.api.card.entity.PhysicalCardStatus;
import com.bukadong.tcg.api.card.repository.PhysicalCardRepository;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PhysicalCardStatusService {

    private final PhysicalCardRepository physicalCardRepository;

    /**
     * NFT 발행 성공 후, 카드의 상태를 MINTED 로 업데이트
     */
    @Transactional
    public void updateStatusToMinted(Long physicalCardId, String secret) {
        PhysicalCard physicalCard = findById(physicalCardId);
        physicalCard.markAsMinted(secret);
    }

    /**
     * NFT 발행 실패 후, 카드의 상태를 FAILED 로 업데이트
     */
    @Transactional
    public void updateStatusToFailed(Long physicalCardId) {
        PhysicalCard physicalCard = findById(physicalCardId);
        physicalCard.markAsFailed();
    }

    private PhysicalCard findById(Long id) {
        return physicalCardRepository.findById(id)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PHYSICAL_CARD_NOT_FOUND));
    }
}
