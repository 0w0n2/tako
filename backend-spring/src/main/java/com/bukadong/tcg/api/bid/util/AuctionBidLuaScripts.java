package com.bukadong.tcg.api.bid.util;

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
 * @PARAM ARGV[1] amount (문자열 숫자)
 * @PARAM ARGV[2] nowEpochSec (정수)
 * @PARAM ARGV[3] idemTtlSec (정수; 권장 1800=30분)
 * @PARAM ARGV[4] payloadOk (성공 이벤트 JSON)
 * @PARAM ARGV[5] payloadMissing (거절: MISSING)
 * @PARAM ARGV[6] payloadNotRunning (거절: NOT_RUNNING)
 * @PARAM ARGV[7] payloadLowPrice (거절: LOW_PRICE)
 * @PARAM ARGV[8] bidderId (입찰자 ID, 정수형 문자열)
 * @PARAM ARGV[9] payloadSelfBid (거절: SELF_BID)
 * @RETURN {code, currentPriceAfter} // code:
 *         OK|DUPLICATE|NOT_RUNNING|LOW_PRICE|MISSING
 */
@UtilityClass
public class AuctionBidLuaScripts {

    public static final String BID_ATOMIC =
            // 멱등
            "if redis.call('EXISTS', KEYS[3]) == 1 then "
                    + "  return {'DUPLICATE', redis.call('HGET', KEYS[1], 'current_price') or ''} " + "end "

                    // 필드 일괄 조회(+ owner_id)
                    + "local vals = redis.call('HMGET', KEYS[1], 'is_end','start_ts','end_ts','current_price','bid_unit','owner_id','buy_now_flag','buy_now_price') "
                    + "local is_end   = vals[1] " + "local startts  = tonumber(vals[2] or '0') "
                    + "local endts    = tonumber(vals[3] or '0') " + "local curStr   = vals[4] "
                    + "local unitStr  = vals[5] " + "local ownerStr = vals[6] " + "local bnFlag   = vals[7] "
                    + "local bnStr    = vals[8] "

                    + "if (not curStr) or (not unitStr) or (startts==0) or (endts==0) or (not ownerStr) then "
                    + "  redis.call('RPUSH', KEYS[2], ARGV[5]); return {'MISSING', curStr or ''} " + "end "

                    // 시간 상태
                    + "local now = tonumber(ARGV[2] or '0') "
                    + "if (is_end=='1') or (now < startts) or (now >= endts) then "
                    + "  redis.call('RPUSH', KEYS[2], ARGV[6]); return {'NOT_RUNNING', curStr} " + "end "

                    // 본인 입찰 금지
                    + "local ownerId  = tonumber(ownerStr or '0') " + "local bidderId = tonumber(ARGV[8] or '0') "
                    + "if ownerId > 0 and bidderId > 0 and (ownerId == bidderId) then "
                    + "  redis.call('RPUSH', KEYS[2], ARGV[9]); return {'SELF_BID', curStr} " + "end "

                    // 1e8 스케일 변환
                    + "local function toInt(s) " + "  if not s then return nil end "
                    + "  local dot = string.find(s, '%.') " + "  if dot then "
                    + "    local intPart = string.sub(s,1,dot-1) "
                    + "    local frac = string.sub(s,dot+1) .. '00000000' " + "    frac = string.sub(frac,1,8) "
                    + "    if intPart=='' then intPart='0' end "
                    + "    return tonumber(intPart)*100000000 + tonumber(frac) " + "  else "
                    + "    return tonumber(s)*100000000 " + "  end " + "end "

                    + "local curI  = toInt(curStr) " + "local unitI = toInt(unitStr) " + "local bidI  = toInt(ARGV[1]) "
                    + "if (not curI) or (not unitI) or (not bidI) then "
                    + "  redis.call('RPUSH', KEYS[2], ARGV[7]); return {'LOW_PRICE', curStr} " + "end "

                    // 즉시구매 처리: buy_now_flag==1이고, bid >= buy_now_price 이면 즉시 구매
                    + "local function isBuyNow() " + "  if (bnFlag == '1') and (bnStr ~= nil) and (bnStr ~= '') then "
                    + "    local bnI = toInt(bnStr) "
                    + "    if (bnI ~= nil) and (bidI >= bnI) then return true, bnStr end " + "  end "
                    + "  return false, nil " + "end "

                    + "local bnOk, bnPriceStr = isBuyNow() " + "if bnOk then "
                    + "  redis.call('HSET', KEYS[1], 'current_price', bnPriceStr) "
                    + "  redis.call('HSET', KEYS[1], 'is_end', '1') " + "  redis.call('RPUSH', KEYS[2], ARGV[10]) "
                    + "  redis.call('SET', KEYS[3], '1', 'EX', tonumber(ARGV[3]) or 1800) "
                    + "  return {'OK', bnPriceStr} " + "end "

                    // 최소 증가
                    + "if bidI < (curI + unitI) then "
                    + "  redis.call('RPUSH', KEYS[2], ARGV[7]); return {'LOW_PRICE', curStr} " + "end "

                    // 성공
                    + "redis.call('HSET', KEYS[1], 'current_price', ARGV[1]); "
                    + "redis.call('RPUSH', KEYS[2], ARGV[4]); "
                    + "redis.call('SET', KEYS[3], '1', 'EX', tonumber(ARGV[3]) or 1800); " + "return {'OK', ARGV[1]} ";
}
