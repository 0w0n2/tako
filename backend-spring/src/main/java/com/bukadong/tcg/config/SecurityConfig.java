package com.bukadong.tcg.config;

import com.bukadong.tcg.properties.security.CorsProperties;
import com.bukadong.tcg.properties.security.OAuth2Properties;
import com.bukadong.tcg.properties.security.RoleProperties;
import com.bukadong.tcg.properties.security.WhitelistProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                /* CORS, CSRF, 폼로그인, 세션 인증 */
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // 커스텀 CORS 설정 적용
                .csrf(AbstractHttpConfigurer::disable)                              // CSRF 보호 비활성화
                .formLogin(AbstractHttpConfigurer::disable)                         // 폼 로그인 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)                         // HTTP Basic 인증 비활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))   // 세션 인증 미사용

                /* 경로별 인가 */

                /* OAuth2 */

                /* 필터 */

                /* Exception */

                .build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {  // 정적 리소스에 대해서 Security Filter 제한하지 않음
        return web -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(corsProperties.allowedOrigins());
        // corsConfiguration.addAllowedOriginPattern("*");
        configuration.setAllowedMethods(corsProperties.allowedMethods());
        configuration.setAllowedHeaders(corsProperties.allowedHeaders());
        configuration.setAllowCredentials(corsProperties.allowCredentials());
        configuration.setExposedHeaders(corsProperties.exposedHeaders());
        configuration.setMaxAge(corsProperties.maxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
