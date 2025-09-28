package com.bukadong.tcg.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AwsS3PresignerConfig {

    @Value("${cloud.aws.credentials.access-key:${AWS_ACCESS_KEY}}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key:${AWS_SECRET_KEY}}")
    private String secretKey;

    @Value("${cloud.aws.region.static:${AWS_REGION_STATIC:ap-northeast-2}}")
    private String region;

    /**
     * S3 프리사인 URL 발급용 Presigner
     */
    @Bean
    S3Presigner s3Presigner() {
        return S3Presigner.builder().region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
}
