package com.bukadong.tcg.api.member.controller;

import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.api.member.dto.AvailabilityResponse;
import com.bukadong.tcg.api.member.service.MemberAvailabilityService;
import com.bukadong.tcg.global.constant.Patterns;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

/**
 * 회원가입 전 이메일/닉네임 가용성 확인
 * <p>
 * 컨트롤러에서 파라미터를 직접 검증하고, 유효하지 않으면 즉시 실패 BaseResponse로 응답한다.
 * </p>
 * URL prefix: /v1/auth/availability
 */
@RestController
@RequestMapping("/v1/auth/availability")
@RequiredArgsConstructor
public class MemberAvailabilityController {

    private final MemberAvailabilityService memberAvailabilityService;

    /**
     * 이메일 중복 확인 - 유효성 실패: INVALID_EMAIL_ADDRESS 로 실패 응답 - 성공: { field, value,
     * available } 반환
     */
    @GetMapping("/email")
    public BaseResponse<AvailabilityResponse> checkEmail(@RequestParam("email") String email) {
        String v = email == null ? "" : email.strip();

        if (!StringUtils.hasText(v) || !Patterns.SIMPLE_EMAIL.matcher(v).matches()) {
            // 실패 BaseResponse(불가능)로 바로 응답
            return new BaseResponse<>(BaseResponseStatus.INVALID_EMAIL_ADDRESS);
        }

        boolean available = memberAvailabilityService.isEmailAvailable(v);
        return new BaseResponse<>(new AvailabilityResponse("email", v, available));
    }

    /**
     * 닉네임 중복 확인 - 유효성 실패: INVALID_PARAMETER 로 실패 응답 - 성공: { field, value, available
     * } 반환
     */
    @GetMapping("/nickname")
    public BaseResponse<AvailabilityResponse> checkNickname(
            @RequestParam("nickname") String nickname) {
        String v = nickname == null ? "" : nickname.strip();

        // 공백/널 또는 패턴 불일치 -> 실패 응답
        if (!StringUtils.hasText(v) || !Patterns.NICKNAME_PATTERN.matcher(v).matches()) {
            return new BaseResponse<>(BaseResponseStatus.INVALID_NICKNAME);
        }

        boolean available = memberAvailabilityService.isNicknameAvailable(v);
        return new BaseResponse<>(new AvailabilityResponse("nickname", v, available));
    }
}
