package com.bukadong.tcg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 스케줄러 사용한다는 뜻
public class TcgApplication {

    public static void main(String[] args) {
        SpringApplication.run(TcgApplication.class, args);
    }
}
