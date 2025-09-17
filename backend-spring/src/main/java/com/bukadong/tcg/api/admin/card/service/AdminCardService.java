package com.bukadong.tcg.api.admin.card.service;

import com.bukadong.tcg.api.admin.card.dto.response.CreateCardRequestDto;
import org.springframework.web.multipart.MultipartFile;

public interface AdminCardService {
    void saveCard(CreateCardRequestDto requestDto, MultipartFile cardImage);
}
