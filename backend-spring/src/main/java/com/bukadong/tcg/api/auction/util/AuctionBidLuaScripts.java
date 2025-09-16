package com.bukadong.tcg.api.auction.util;

import lombok.experimental.UtilityClass;

/**
 * 입찰 원자 처리 Lua 스크립트
 * <P>
 * 경매 상태 검증 + 현재가 갱신 + 이벤트 적재 + 멱등키 설정을 원자적으로 수행한다.
 * 실패(LOW_PRICE/NOT_RUNNING/MISSING)도 거절 이벤트를 큐에 넣는다.
 * </P>
 * 
 * @PARAM KEYS[1] auctionHashKey (e.g., "auction:{id}")
 * @PARAM KEYS[2] bidQueueKey (e.g., "auction:{id}:bidq")
 * @PARAM KEYS[3] idemKey (e.g., "idem:{requestId}")
 * @PARAM ARGV[1] bidPrice (문자열 숫자)
 * @PARAM ARGV[2] nowEpochSec (정수)
 * @PARAM ARGV[3] idemTtlSec (정수)
 * @PARAM ARGV[4] payloadOk (성공 이벤트 JSON)
 * @PARAM ARGV[5] payloadReject (거절 이벤트 JSON; reason은 컨슈머에서 map)
 * @RETURN {code, currentPriceAfter} // code:
 *         OK|DUPLICATE|NOT_RUNNING|LOW_PRICE|MISSING
 */
@UtilityClass
public class AuctionBidLuaScripts {
    public static final String BID_ATOMIC =
            // 멱등키 확인
            "if redis.call('EXISTS', KEYS[3]) == 1 then "
                    + "  return {'DUPLICATE', redis.call('HGET', KEYS[1], 'current_price') or ''} " + "end " +

                    // 상태 읽기
                    "local is_end  = redis.call('HGET', KEYS[1], 'is_end') "
                    + "local startts = tonumber(redis.call('HGET', KEYS[1], 'start_ts')) "
                    + "local endts   = tonumber(redis.call('HGET', KEYS[1], 'end_ts')) "
                    + "local cur     = redis.call('HGET', KEYS[1], 'current_price') "
                    + "local unit    = redis.call('HGET', KEYS[1], 'bid_unit') "
                    + "if not cur or not unit or not startts or not endts then "
                    + "  redis.call('RPUSH', KEYS[2], ARGV[5]) " + // 거절 이벤트 적재(MISSING)
                    "  return {'MISSING', cur or ''} " + "end " +

                    "local now = tonumber(ARGV[2]) " + "if (is_end == '1') or (now < startts) or (now > endts) then "
                    + "  redis.call('RPUSH', KEYS[2], ARGV[5]) " + // 거절 이벤트 적재(NOT_RUNNING)
                    "  return {'NOT_RUNNING', cur} " + "end " +

                    // 최소 허용가: current + unit
                    "local curN  = tonumber(cur) " + "local unitN = tonumber(unit) "
                    + "local bidN  = tonumber(ARGV[1]) " + "if not curN or not unitN or not bidN then "
                    + "  redis.call('RPUSH', KEYS[2], ARGV[5]) " + "  return {'LOW_PRICE', cur} " + "end "
                    + "if bidN < (curN + unitN) then " + "  redis.call('RPUSH', KEYS[2], ARGV[5]) "
                    + "  return {'LOW_PRICE', cur} " + "end " +

                    // 성공: 현재가 갱신 + 성공 이벤트 적재 + 멱등키
                    "redis.call('HSET', KEYS[1], 'current_price', ARGV[1]) " + "redis.call('RPUSH', KEYS[2], ARGV[4]) "
                    + "redis.call('SET', KEYS[3], '1', 'EX', tonumber(ARGV[3])) " + "return {'OK', ARGV[1]} ";
}
