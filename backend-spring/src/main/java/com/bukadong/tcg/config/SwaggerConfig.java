package com.bukadong.tcg.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${swagger.uri}")
    private String swaggerUri;

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI().addServersItem(new Server().url(swaggerUri))
                .info(new Info().title("SSAFY TCG 옥션의 API").description("00 API 테스트를 위한 Swagger UI")
                        .version("1.0.0"));
    }

    @Bean
    GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder().group("public-api") // 그룹 이름
                .pathsToMatch("/v1/**") // 이 경로에 해당하는 API 문서화
                .build();
    }
}