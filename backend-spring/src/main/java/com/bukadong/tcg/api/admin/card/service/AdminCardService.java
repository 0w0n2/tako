package com.bukadong.tcg.api.admin.card.service;

import com.bukadong.tcg.api.admin.card.dto.response.CreateCardRequestDto;
import com.bukadong.tcg.api.card.entity.Card;

public interface AdminCardService {
    Card saveCard(CreateCardRequestDto requestDto);
}
