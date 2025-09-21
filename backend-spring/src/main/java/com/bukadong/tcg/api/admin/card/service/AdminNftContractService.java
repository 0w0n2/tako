package com.bukadong.tcg.api.admin.card.service;

import com.bukadong.tcg.api.admin.card.dto.request.AdminNftCreateRequestDto;
import com.bukadong.tcg.api.admin.card.dto.response.AdminNftCreateResponseDto;

public interface AdminNftContractService {
    AdminNftCreateResponseDto requestNftCreation(AdminNftCreateRequestDto requestDto);
}
