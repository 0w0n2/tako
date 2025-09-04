package com.bukadong.tcg.redis.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 연결/Template 설정
 */
@Configuration
@EnableRedisRepositories(basePackages = "com.bukadong.tcg")
@Log4j2
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    /**
     * 비밀번호가 없을 수도 있으므로 기본값은 빈 문자열로 둡니다.
     * application.yml에서 기본값을 지정했다면 굳이 :""는 없어도 됩니다.
     */
    @Value("${spring.data.redis.password:}")
    private String password;

    @Bean
    RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration();
        conf.setHostName(host);
        conf.setPort(port);
        if (!password.isEmpty()) {
            conf.setPassword(RedisPassword.of(password)); // 권장 방식
        }
        return new LettuceConnectionFactory(conf);
    }

    @Bean
    @Primary
    RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> t = new RedisTemplate<>();
        t.setKeySerializer(new StringRedisSerializer());
        t.setValueSerializer(new StringRedisSerializer());
        t.setConnectionFactory(redisConnectionFactory());
        return t;
    }
}
