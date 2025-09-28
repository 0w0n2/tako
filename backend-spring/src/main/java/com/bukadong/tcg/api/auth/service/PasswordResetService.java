package com.bukadong.tcg.api.auth.service;

import com.bukadong.tcg.api.auth.dto.request.PasswordResetRequestDto;
import org.springframework.transaction.annotation.Transactional;

public interface PasswordResetService {
    @Transactional
    void updatePasswordWithResetCode(PasswordResetRequestDto requestDto);
}
