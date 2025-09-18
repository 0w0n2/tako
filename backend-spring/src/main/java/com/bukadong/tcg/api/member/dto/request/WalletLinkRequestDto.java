package com.bukadong.tcg.api.member.dto.request;

import static com.bukadong.tcg.global.constant.ErrorMessages.*;
import static com.bukadong.tcg.global.constant.Patterns.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 지갑 주소 연동 요청 DTO
 */
public record WalletLinkRequestDto(
        @NotBlank(message = WALLET_ADDRESS_NOT_FOUND)
        @Pattern(regexp = WALLET_ADDRESS_REGEX, message = INVALID_WALLET_ADDRESS)
        String walletAddress
) {
}
