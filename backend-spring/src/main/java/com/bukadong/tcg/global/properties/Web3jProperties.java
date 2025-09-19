package com.bukadong.tcg.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "block-chain")
public record Web3jProperties(
    String sepoliaRpcUrl,
    String sepoliaPrivateKey
) {
}
