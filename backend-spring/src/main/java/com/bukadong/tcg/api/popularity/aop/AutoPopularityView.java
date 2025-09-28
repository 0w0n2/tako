package com.bukadong.tcg.api.popularity.aop;

import java.lang.annotation.*;

/**
 * 인기: 조회 카운팅 자동 트리거
 * <P>
 * 메서드가 성공하면 경매 상세 조회로 간주하고 대분류(major) 기준으로 카운팅한다.
 * </P>
 * 
 * @RETURN 없음
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoPopularityView {
}
