package com.bukadong.tcg.api.auth.service;

import com.bukadong.tcg.api.auth.dto.request.SignUpRequestDto;
import com.bukadong.tcg.api.member.entity.Member;

public interface SignUpService {
    Member signUp(SignUpRequestDto requestDto);
}
