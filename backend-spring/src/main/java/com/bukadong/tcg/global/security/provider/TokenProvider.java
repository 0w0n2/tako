package com.bukadong.tcg.global.security.provider;

import com.bukadong.tcg.api.member.repository.MemberRepository;

import static com.bukadong.tcg.global.common.base.BaseResponseStatus.*;

import com.bukadong.tcg.global.common.exception.BaseException;
import com.bukadong.tcg.global.security.dto.UserDetailsDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static com.bukadong.tcg.global.constant.SecurityConstants.*;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TokenProvider {

    private final SecretKey secretKey;
    private final MemberRepository memberRepository;

    public TokenProvider(@Value("${security.jwt.secret-key}") String secretKey, MemberRepository memberRepository) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        this.memberRepository = memberRepository;
    }

    public String generateAccessToken(String subject, String authorities, Date expiration) {
        return Jwts.builder()
                .subject(subject)
                .claim(AUTHORITIES_CLAIM, authorities)
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

    /* JWT 토큰을 복호화하여 토큰에 포함된 클레임(Payload) 반환 */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (SecurityException | MalformedJwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT Token: {}", e.getMessage());
            throw new BaseException(INVALID_JWT_TOKEN);
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT Token: {}", e.getMessage());
            throw new BaseException(EXPIRED_JWT_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT Token: {}", e.getMessage());
            throw new BaseException(UNSUPPORTED_JWT_TOKEN);
        }
    }

    /* 토큰 유효성 검증 */
    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        try {
            parseClaims(token);
            return true;
        } catch (BaseException e) {
            return false;
        }
    }

    /* Request Header 의 'Authorization' 필드에서 토큰 정보 추출 */
    public String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(GRANT_TYPE)) {
            return bearerToken.substring(GRANT_TYPE.length());
        }
        return null;
    }

    /* JWT 토큰 정보 바탕으로 Authentication 객체를 생성하여 반환 */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        if (claims.get(AUTHORITIES_CLAIM) == null) {
            throw new BaseException(INVALID_TOKEN_CLAIM);
        }

        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get(AUTHORITIES_CLAIM).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        String subject = claims.getSubject();

        UserDetails principal = memberRepository.findByEmail(subject)
                .map(UserDetailsDto::new)
                .orElseThrow(() -> new UsernameNotFoundException("해당하는 사용자를 찾을 수 없습니다."));

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    /* 토큰에서 subject 정보 추출 */
    public String getSubjectFromToken(String token) {
        try {
            if (!StringUtils.hasText(token)) {
                throw new BaseException(AUTHENTICATION_REQUIRED);
            }
            return parseClaims(token).getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();  // 만료된 토큰에서도 claims 정보 파싱
        }
    }
}
