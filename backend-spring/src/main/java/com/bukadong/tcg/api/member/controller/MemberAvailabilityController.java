package com.bukadong.tcg.api.member.controller;

import com.bukadong.tcg.global.common.base.BaseResponse;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.api.member.dto.AvailabilityResponse;
import com.bukadong.tcg.api.member.service.MemberAvailabilityService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "Auth-Availability", description = "회원가입 전 이메일/닉네임 가용성 확인 API")
public class MemberAvailabilityController {

    private final MemberAvailabilityService memberAvailabilityService;

    // 매우 느슨한 이메일 형식 검증(실무에서는 더 엄격한 정책/화이트리스트 필요할 수 있음)
    private static final Pattern SIMPLE_EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    // 영문/한글/숫자만 허용, 길이 2~30
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[0-9A-Za-z가-힣]{2,30}$");

    /**
     * 이메일 중복 확인 - 유효성 실패: INVALID_EMAIL_ADDRESS 로 실패 응답 - 성공: { field, value,
     * available } 반환
     */
    @Operation(summary = "이메일 가용성 확인", description = "이메일 형식을 검증하고, 사용 가능 여부를 반환합니다.")
    @GetMapping("/email")
    public BaseResponse<AvailabilityResponse> checkEmail(
            @Parameter(description = "확인할 이메일 주소") @RequestParam("email") String email) {
        String v = email == null ? "" : email.strip();

        if (!StringUtils.hasText(v) || !SIMPLE_EMAIL.matcher(v).matches()) {
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
            @Parameter(description = "확인할 닉네임") @RequestParam("nickname") String nickname) {
        String v = nickname == null ? "" : nickname.strip();

        // 공백/널 또는 패턴 불일치 -> 실패 응답
        if (!StringUtils.hasText(v) || !NICKNAME_PATTERN.matcher(v).matches()) {
            return new BaseResponse<>(BaseResponseStatus.INVALID_NICKNAME);
        }

        boolean available = memberAvailabilityService.isNicknameAvailable(v);
        return new BaseResponse<>(new AvailabilityResponse("nickname", v, available));
    }
}
