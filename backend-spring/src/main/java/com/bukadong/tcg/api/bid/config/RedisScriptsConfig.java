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
 * @RETURN DefaultRedisScript<List>
 */
@Configuration
public class RedisScriptsConfig {

    /**
     * 입찰 원자 처리 스크립트
     * <P>
     * 반환 형식: [code, currentPriceAfter]
     * </P>
     * 
     * @RETURN DefaultRedisScript<List>
     */
    @Bean
    public DefaultRedisScript<List> bidAtomicScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptText(AuctionBidLuaScripts.BID_ATOMIC);
        script.setResultType(List.class);
        return script;
    }
}
