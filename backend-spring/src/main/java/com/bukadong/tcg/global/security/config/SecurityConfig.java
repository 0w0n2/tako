package com.bukadong.tcg.global.security.config;

import com.bukadong.tcg.api.member.entity.Role;
import com.bukadong.tcg.global.security.filter.BaseExceptionHandlerFilter;
import com.bukadong.tcg.global.properties.SecurityCorsProperties;
import com.bukadong.tcg.global.properties.SecurityOAuth2Properties;
import com.bukadong.tcg.global.properties.SecurityRoleProperties;
import com.bukadong.tcg.global.properties.SecurityWhitelistProperties;
import com.bukadong.tcg.global.security.filter.JwtAuthenticationFilter;
import com.bukadong.tcg.global.security.handler.CustomAccessDeniedHandler;
import com.bukadong.tcg.global.security.handler.CustomAuthenticationEntryPoint;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security 인증/인가 환경 설정 담당 클래스
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties({
        SecurityCorsProperties.class,
        SecurityOAuth2Properties.class,
        SecurityWhitelistProperties.class,
        SecurityRoleProperties.class
})
@RequiredArgsConstructor
public class SecurityConfig {

    /* properties */
    private final SecurityCorsProperties corsProperties;
    private final SecurityOAuth2Properties oAuth2Properties;
    private final SecurityWhitelistProperties whitelistProperties;
    private final SecurityRoleProperties roleProperties;

    /* filter */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final BaseExceptionHandlerFilter baseExceptionHandlerFilter;

    /* handler */
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                /* 기본 보안 설정 */
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // 커스텀 CORS 설정 적용
                .csrf(AbstractHttpConfigurer::disable)                              // CSRF 보호 비활성화
                .formLogin(AbstractHttpConfigurer::disable)                         // 폼 로그인 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)                         // HTTP Basic 인증 비활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))   // 세션 인증 미사용

                /* 경로별 인가 규칙 설정 */
                .authorizeHttpRequests(auth -> {
                    whitelistProperties.getParsedWhitelist().forEach((method, urls) -> {
                        if (urls != null && !urls.isEmpty()) {
                            String[] urlPatterns = urls.stream()
                                    .filter(StringUtils::hasText)
                                    .map(String::trim)
                                    .toArray(String[]::new);
                            if (urlPatterns.length > 0) {
                                auth.requestMatchers(method, urlPatterns).permitAll();
                            }
                        }
                    });
                    auth.requestMatchers(roleProperties.admin().toArray(new String[0])).hasAuthority(Role.ADMIN.getRoleName());
                    auth.anyRequest().authenticated();
                })

                /* OAuth2 (추후 확장) */
                // TODO-SECURITY: oauth2 관련 설정 추가

                /* 커스텀 필터 등록 */
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(baseExceptionHandlerFilter, JwtAuthenticationFilter.class)

                /* Exception Handler */
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint) // 인증 실패(401)
                        .accessDeniedHandler(accessDeniedHandler)           // 인가 실패(403)
                )
                .build();
    }

    /* 정적 리소스(js, css, image 등)에 대해 Spring Security 필터 체인을 적용하지 않도록 설정 */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /* CORS 정책 설정 */
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
