package com.bukadong.tcg.api.popularity.aop;

import java.lang.annotation.*;

/**
 * AOP에서 auctionId 파라미터를 명시적으로 지정하기 위한 마커 애노테이션.
 * <P>
 * 컨트롤러 메서드 파라미터(Long)에 붙여 사용한다.
 * </P>
 * 
 * @RETURN 없음
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PopularityAuctionId {
}
