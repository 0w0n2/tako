package com.bukadong.tcg.global.config;

import com.bukadong.tcg.global.properties.Web3jProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

/**
 * Web3j 환경 설정 담당 클래스
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({Web3jProperties.class})
public class Web3jConfig {

    /* properties */
    private final Web3jProperties web3jProperties;

    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(web3jProperties.sepoliaRpcUrl()));
    }
}
