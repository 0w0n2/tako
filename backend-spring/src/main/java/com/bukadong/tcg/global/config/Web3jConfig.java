package com.bukadong.tcg.global.config;

import com.bukadong.tcg.global.properties.blockchain.BlockChainProperties;
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
@EnableConfigurationProperties({BlockChainProperties.class})
public class Web3jConfig {

    /* properties */
    private final BlockChainProperties blockChainProperties;

    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(blockChainProperties.sepolia().rpcUrl()));
    }
}
