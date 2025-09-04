package com.bukadong.tcg.auth.controller;

import com.bukadong.tcg.common.base.BaseResponse;
import com.bukadong.tcg.member.dto.AvailabilityResponse;
import com.bukadong.tcg.member.service.MemberAvailabilityService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 회원가입 전 이메일/닉네임 가용성 확인
 * <p>
 * 공통 응답 포맷(BaseResponse)을 사용합니다.
 * </p>
 * URL prefix: /api/v1/auth/availability
 */
@RestController
@RequestMapping("/api/v1/auth/availability")
@RequiredArgsConstructor
@Validated
public class MemberAvailabilityController {

    private final MemberAvailabilityService memberAvailabilityService;

    /**
     * 이메일 중복 확인
     * 
     * @param email 확인할 이메일
     * @return { field: "email", value: "...", available: true|false }
     */
    @GetMapping("/email")
    public BaseResponse<AvailabilityResponse> checkEmail(@RequestParam("email") @Email String email) {
        boolean available = memberAvailabilityService.isEmailAvailable(email);
        return new BaseResponse<>(new AvailabilityResponse("email", email, available));
    }

    /**
     * 닉네임 중복 확인
     * 
     * @param nickname 확인할 닉네임(2~30자)
     * @return { field: "nickname", value: "...", available: true|false }
     */
    @GetMapping("/nickname")
    public BaseResponse<AvailabilityResponse> checkNickname(
            @RequestParam("nickname") @Size(min = 2, max = 30) String nickname) {
        boolean available = memberAvailabilityService.isNicknameAvailable(nickname);
        return new BaseResponse<>(new AvailabilityResponse("nickname", nickname, available));
    }
}
