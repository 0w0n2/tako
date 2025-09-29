package com.bukadong.tcg.api.member.dto.request;

import static com.bukadong.tcg.global.constant.ErrorMessages.*;
import static com.bukadong.tcg.global.constant.Patterns.*;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 지갑 주소 연동 요청 DTO
 */
public record WalletLinkRequestDto(
        @Schema(description = "지갑 주소", example = "0xE6a1234F242E312347Ee0aFf801234fC78d08a9")
        @NotBlank(message = WALLET_ADDRESS_NOT_FOUND)
        @Pattern(regexp = WALLET_ADDRESS_REGEX, message = INVALID_WALLET_ADDRESS)
        String walletAddress
) {
}
