package com.bukadong.tcg.config;

import com.bukadong.tcg.properties.security.CorsProperties;
import com.bukadong.tcg.properties.security.OAuth2Properties;
import com.bukadong.tcg.properties.security.RoleProperties;
import com.bukadong.tcg.properties.security.WhitelistProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * Spring Security 인증/인가 환경 설정 Configuration 클래스
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties({CorsProperties.class, OAuth2Properties.class, WhitelistProperties.class, RoleProperties.class})
@RequiredArgsConstructor
public class SecurityConfig {

    /* properties */
    private final CorsProperties corsProperties;
    private final OAuth2Properties oAuth2Properties;
    private final WhitelistProperties whitelistProperties;
    private final RoleProperties roleProperties;

    /* service, handler */

}
