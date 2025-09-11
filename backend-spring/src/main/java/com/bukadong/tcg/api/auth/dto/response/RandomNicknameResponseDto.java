package com.bukadong.tcg.api.auth.dto.response;

import lombok.Builder;

@Builder
public record RandomNicknameResponseDto(String nickname) {
    public static RandomNicknameResponseDto toDto(String nickname) {
        return RandomNicknameResponseDto.builder().nickname(nickname).build();
    }
}
