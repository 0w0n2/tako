package com.bukadong.tcg.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String BEARER_TOKEN_PREFIX = "Bearer";

    @Value("${swagger.uri}")
    private String swaggerUri;

    @Bean
    OpenAPI openAPI() {
        String securityJwtName = "JWT";

        SecurityRequirement securityRequirement =
                new SecurityRequirement().addList(securityJwtName);
        Components components = new Components().addSecuritySchemes(securityJwtName,
                new SecurityScheme().name(securityJwtName).type(SecurityScheme.Type.HTTP)
                        .scheme(BEARER_TOKEN_PREFIX).bearerFormat(securityJwtName));

        return new OpenAPI().addSecurityItem(securityRequirement).components(components)
                .addServersItem(new Server().url(swaggerUri)).info(info());
    }

    private Info info() {
            return new Info().title("SSAFY TCG 옥션의 API")
                    .description("00 API 테스트를 위한 Swagger UI")
                    .version("1.0.0");
    }

    @Bean
    GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder().group("public-api") // 그룹 이름
                .pathsToMatch("/v1/**") // 이 경로에 해당하는 API 문서화
                .build();
    }
}