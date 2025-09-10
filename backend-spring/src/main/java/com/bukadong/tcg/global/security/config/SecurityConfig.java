package com.bukadong.tcg.global.security.config;

import com.bukadong.tcg.api.member.entity.Role;
import com.bukadong.tcg.global.common.exception.BaseExceptionHandlerFilter;
import com.bukadong.tcg.global.properties.SecurityCorsProperties;
import com.bukadong.tcg.global.properties.SecurityOAuth2Properties;
import com.bukadong.tcg.global.properties.SecurityRoleProperties;
import com.bukadong.tcg.global.properties.SecurityWhitelistProperties;
import com.bukadong.tcg.global.security.filter.JwtAuthenticationFilter;
import com.bukadong.tcg.global.security.service.JwtTokenBlackListService;
import com.bukadong.tcg.global.util.JwtTokenUtils;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security 인증/인가 환경 설정 Configuration 클래스
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties({SecurityCorsProperties.class, SecurityOAuth2Properties.class, SecurityWhitelistProperties.class, SecurityRoleProperties.class})
@RequiredArgsConstructor
public class SecurityConfig {

    /* properties */
    private final SecurityCorsProperties corsProperties;
    private final SecurityOAuth2Properties oAuth2Properties;
    private final SecurityWhitelistProperties whitelistProperties;
    private final SecurityRoleProperties roleProperties;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final BaseExceptionHandlerFilter baseExceptionHandlerFilter;

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
                .authorizeHttpRequests(auth -> {
                    whitelistProperties.getParsedWhitelist().forEach((method, urls) -> {
                        if (urls != null && !urls.isEmpty()) {
                            auth.requestMatchers(method, urls.toArray(new String[0])).permitAll();
                        }
                    });
                    auth.requestMatchers(roleProperties.admin().toArray(new String[0])).hasRole(Role.ADMIN.getRoleName());
                    auth.anyRequest().authenticated();
                })

                /* OAuth2 */
                // TODO-SECURITY: oauth2 관련 설정 추가

                /* 필터 */
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(baseExceptionHandlerFilter, JwtAuthenticationFilter.class)

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
