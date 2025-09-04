package org.test.be.config;

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
        return new OpenAPI()
                .addServersItem(new Server().url(swaggerUri))
                .info(new Info()
                        .title("SmartContract Test BE API")
                        .description("블록체인 스마트 컨트랙트 기능 테스트를 위한 Swagger UI")
                        .version("1.0.0"));
    }

    @Bean
    GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public-api") // 그룹 이름
                .pathsToMatch("/api/v1/**") // 이 경로에 해당하는 API 문서화
                .build();
    }
}