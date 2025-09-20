package com.bukadong.tcg.global.properties.blockchain;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "block-chain")
public record BlockChainProperties(
    SepoliaProperties sepolia,
    ContractAddressProperties contractAddress
) {

}
