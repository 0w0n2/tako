package com.bukadong.tcg.api.popularity.aop;

import com.bukadong.tcg.api.auction.repository.AuctionRepository;
import com.bukadong.tcg.api.popularity.service.PopularityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Optional;

/**
 * 인기 자동 카운팅 AOP(대분류 전용)
 * <P>
 * 주석으로 표시된 메서드가 성공하면 auctionId에서 categoryMajor.id를 찾아 카운팅한다.
 * </P>
 * 
 * @RETURN 없음
 */
@Log4j2
@Aspect
@Component
@RequiredArgsConstructor
public class PopularityAutoAspect {

    private final PopularityService popularityService;
    private final AuctionRepository auctionRepository;

    /**
     * 조회 후킹
     * <P>
     * 대분류 기준으로 recordView를 호출한다.
     * </P>
     * 
     * @PARAM jp 조인포인트
     * @RETURN 없음
     */
    @AfterReturning("@annotation(com.bukadong.tcg.api.popularity.aop.AutoPopularityView)")
    public void afterView(JoinPoint jp) {
        triggerCount(jp, false);
    }

    /**
     * 입찰 후킹
     * <P>
     * 대분류 기준으로 recordBid를 호출한다.
     * </P>
     * 
     * @PARAM jp 조인포인트
     * @RETURN 없음
     */
    @AfterReturning("@annotation(com.bukadong.tcg.api.popularity.aop.AutoPopularityBid)")
    public void afterBid(JoinPoint jp) {
        triggerCount(jp, true);
    }

    /**
     * 핵심 트리거 (대분류 전용)
     * <P>
     * auctionId → categoryMajor.id 조회 → 카운팅 호출.
     * </P>
     * 
     * @PARAM jp 조인포인트
     * @PARAM isBid 입찰 여부
     * @RETURN 없음
     */
    private void triggerCount(JoinPoint jp, boolean isBid) {
        try {
            Long auctionId = resolveAuctionId(jp);
            if (auctionId == null) {
                log.debug("[PopularityAOP] auctionId not found in method args: {}",
                        jp.getSignature());
                return;
            }

            Optional<Long> majorIdOpt = auctionRepository.findCategoryMajorIdByAuctionId(auctionId);
            if (majorIdOpt.isEmpty()) {
                log.debug("[PopularityAOP] categoryMajorId not found for auctionId={}", auctionId);
                return;
            }

            long categoryMajorId = majorIdOpt.get();
            if (isBid) {
                popularityService.recordBid(categoryMajorId, auctionId);
            } else {
                popularityService.recordView(categoryMajorId, auctionId);
            }
        } catch (Exception e) {
            // 본 요청 흐름에 영향 주지 않음
            log.warn("[PopularityAOP] counting skipped: {}", e.getMessage());
        }
    }

    /**
     * 메서드 인자에서 auctionId 추출
     * <P>
     * @PathVariable("auctionId") 우선, 없으면 첫 Long 타입 인자 사용.
     * </P>
     * 
     * @PARAM jp 조인포인트
     * @RETURN auctionId 또는 null
     */
    private Long resolveAuctionId(JoinPoint jp) {
        Object[] args = jp.getArgs();
        Parameter[] params = ((MethodSignature) jp.getSignature()).getMethod().getParameters();

        for (int i = 0; i < params.length; i++) {
            for (Annotation an : params[i].getAnnotations()) {
                if (an instanceof PathVariable pv) {
                    String name = pv.value().isEmpty() ? params[i].getName() : pv.value();
                    if ("auctionId".equals(name) && args[i] instanceof Long l) {
                        return l;
                    }
                }
            }
        }
        for (Object arg : args) {
            if (arg instanceof Long l)
                return l;
        }
        return null;
    }
}
