package com.bukadong.tcg.global.util;

import com.bukadong.tcg.api.member.repository.MemberRepository;
import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenUtils {

    private final SecretKey secretKey;
    private final MemberRepository memberRepository;

    public JwtTokenUtils(@Value("${security.jwt.secret-key}") SecretKey secretKey, MemberRepository memberRepository) {
        this.secretKey = secretKey;
        this.memberRepository = memberRepository;
    }

    public String generateAccessToken(String subject, String authorities, Date expiration) {
        return Jwts.builder()
                .subject(subject)
                .claim("auth", authorities)
                .expiration(expiration)
                .issuedAt(new Date())
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(String subject, Date expiration) {
        return Jwts.builder()
                .subject(subject)
                .expiration(expiration)
                .issuedAt(new Date())
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /* JWT 토큰 복호화, 파싱 */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (SecurityException | MalformedJwtException e) {
            throw new BaseException(BaseResponseStatus.INV)
        }

    }

    /* 토큰 정보 검증 */
    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
