package com.bukadong.tcg.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.web-url")
public record WebUrlProperties(
        String main,
        String logo
) {
}
