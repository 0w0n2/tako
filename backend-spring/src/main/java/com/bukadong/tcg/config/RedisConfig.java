package com.bukadong.tcg.config;

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
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
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
    RedisTemplate<String, Object> redisTemplate() {
        // Redis와 통신할 템플릿 설정
        RedisTemplate<String, Object> t = new RedisTemplate<>();
        t.setConnectionFactory(redisConnectionFactory());

        // 식렬화 방법 설정
        t.setKeySerializer(new StringRedisSerializer());
        t.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        t.setHashKeySerializer(new StringRedisSerializer());
        t.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));

        // 트랜잭션 설정
        t.setEnableTransactionSupport(true);

        return t;
    }
}
