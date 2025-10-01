package com.bukadong.tcg.api.bid.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import com.bukadong.tcg.api.bid.util.AuctionBidLuaScripts;

import java.util.List;

/**
 * Redis Lua 스크립트 설정
 * <P>
 * 애플리케이션 시작 시 스크립트를 빈으로 등록하여 재사용(EVAL 파싱/전송 오버헤드 감소).
 * </P>
 * 
 * @PARAM 없음
 * @RETURN DefaultRedisScript<List<String>>
 */
@Configuration
public class RedisScriptsConfig {

    /**
     * 입찰 원자 처리 스크립트
     * <P>
     * 반환 형식: [code, currentPriceAfter]
     * </P>
     * 
     * @RETURN DefaultRedisScript<List<String>>
     */
    @Bean
    DefaultRedisScript<List<String>> bidAtomicScript() {
        DefaultRedisScript<List<String>> script = new DefaultRedisScript<>();
        script.setScriptText(AuctionBidLuaScripts.BID_ATOMIC);
        // Redis가 배열로 반환하므로 List<String> 결과 타입으로 설정 (제네릭 소거 보완 캐스팅)
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Class<List<String>> listStringClass = (Class) List.class;
        script.setResultType(listStringClass);
        return script;
    }
}
