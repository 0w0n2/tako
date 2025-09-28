package com.bukadong.tcg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.TimeZone;

@EnableAsync
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling // 스케줄러 사용한다는 뜻
public class TcgApplication {

    public static void main(String[] args) {
        // Force JVM default timezone to UTC to ensure consistent time handling
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(TcgApplication.class, args);
    }
}
