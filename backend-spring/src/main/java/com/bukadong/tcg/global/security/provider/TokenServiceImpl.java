package com.bukadong.tcg.global.security.provider;

import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import com.bukadong.tcg.global.constant.SecurityConstants;
import com.bukadong.tcg.global.security.dto.JwtToken;
import com.bukadong.tcg.global.security.dto.CustomUserDetails;
import com.bukadong.tcg.global.security.dto.MemberPayload;
import com.bukadong.tcg.global.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JWT 토큰의 생성, 재발급, 삭제 등 토큰 관련 비즈니스 로직 처리
 */
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final TokenProvider tokenProvider;
    private final RedisUtils redisUtils;
    private final TokenBlackListService tokenBlackListService;

    @Value("${security.jwt.expire-time.access-token}")
    private Duration accessExpiration;

    @Value("${security.jwt.expire-time.refresh-token}")
    private Duration refreshExpiration;

    @Override
    public JwtToken generateToken(CustomUserDetails userDetails) {
        long now = System.currentTimeMillis();

        String memberUuid = userDetails.getUuid();
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        /* AccessToken 생성 */
        Date accessExpirationDate = new Date(now + accessExpiration.toMillis());
        String accessToken = tokenProvider.generateAccessToken(memberUuid, authorities, accessExpirationDate);

        /* RefreshToken 생성 */
        String refreshToken = generateAndStoreRefreshToken(memberUuid, authorities);

        return JwtToken.builder()
                .grantType(SecurityConstants.GRANT_TYPE.trim())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public JwtToken refresh(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken) || tokenBlackListService.isBlacklistRefreshToken(refreshToken)) {
            throw new BaseException(BaseResponseStatus.INVALID_JWT_TOKEN);
        }

        String refreshUuid = tokenProvider.getSubjectFromToken(refreshToken);
        String redisKey = SecurityConstants.REFRESH_PREFIX + refreshUuid;

        MemberPayload memberPayload = redisUtils.getValue(redisKey, MemberPayload.class)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_JWT_TOKEN));

        String memberUuid = memberPayload.memberUuid();
        String authorities = memberPayload.authorities();

        long now = System.currentTimeMillis();
        Date accessExpirationDate = new Date(now + accessExpiration.toMillis());
        String newAccessToken = tokenProvider.generateAccessToken(memberUuid, authorities, accessExpirationDate);

        deleteRefreshToken(memberUuid);   // 기존 refreshToken은 제거

        String newRefreshToken = generateAndStoreRefreshToken(memberUuid, authorities);

        return JwtToken.builder()
                .grantType(SecurityConstants.GRANT_TYPE.trim())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    public void deleteRefreshToken(String memberUuid) {
        if (!StringUtils.hasText(memberUuid)) {
            throw new BaseException(BaseResponseStatus.INVALID_TOKEN_CLAIM);
        }
        String mappingKey = SecurityConstants.REFRESH_MEMBER_MAPPING_PREFIX + memberUuid;
        redisUtils.getValue(mappingKey, String.class)
                .ifPresent(refreshUUid -> {
                    String redisKey = SecurityConstants.REFRESH_PREFIX + refreshUUid;
                    redisUtils.deleteValue(redisKey);
                    redisUtils.deleteValue(mappingKey);
                });
    }

    private String generateAndStoreRefreshToken(String memberUuid, String authorities) {
        long now = System.currentTimeMillis();
        Date refreshExpirationDate = new Date(now + refreshExpiration.toMillis());
        String refreshTokenUuid = UUID.randomUUID().toString();
        String refreshToken = tokenProvider.generateRefreshToken(refreshTokenUuid, refreshExpirationDate);

        MemberPayload memberPayload = new MemberPayload(memberUuid, authorities);

        redisUtils.setValue(SecurityConstants.REFRESH_PREFIX + refreshTokenUuid, memberPayload, refreshExpiration);
        redisUtils.setValue(SecurityConstants.REFRESH_MEMBER_MAPPING_PREFIX + memberUuid, refreshTokenUuid, refreshExpiration);

        return refreshToken;
    }
}
