package com.bukadong.tcg.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * QueryDSL 설정
 * <p>
 * JPAQueryFactory 빈을 등록한다.
 * </p>
 *
 * @param 없음
 * @return JPAQueryFactory
 */
@Configuration
@ComponentScan(basePackages = "com.bukadong.tcg")
public class QueryDslConfig {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * JPAQueryFactory 빈 등록
     *
     * @param em EntityManager
     * @return JPAQueryFactory
     */
    @Bean
    JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

}
