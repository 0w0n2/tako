package com.bukadong.tcg.api.admin.card.service;

import com.bukadong.tcg.api.admin.card.dto.response.PhysicalCardStatusResponseDto;

public interface AdminPhysicalCardService {
    PhysicalCardStatusResponseDto getPhysicalCardStatus(Long physicalCardId);
}
