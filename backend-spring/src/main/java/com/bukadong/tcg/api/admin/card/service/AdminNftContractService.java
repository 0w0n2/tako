package com.bukadong.tcg.api.admin.card.service;

import com.bukadong.tcg.api.admin.card.dto.response.NftCreateResponseDto;

public interface AdminNftContractService {
    NftCreateResponseDto requestNftCreation(Long cardId);
}
