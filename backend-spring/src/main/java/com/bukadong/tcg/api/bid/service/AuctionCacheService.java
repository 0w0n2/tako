package com.bukadong.tcg.api.bid.service;

import com.bukadong.tcg.api.auction.entity.Auction;
import com.bukadong.tcg.api.auction.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.Map;
import java.math.BigDecimal;
import java.util.HashMap;

/**
 * 경매 캐시 서비스
 * <P>
 * DB → Redis 해시 초기화/보정. Lua 스크립트가 참조하는 필드를 채운다.
 * </P>
 * 
 * @PARAM auctionId 경매 ID
 * @RETURN 없음
 */
// 변경 이후 코드의 코드블럭만
@Service
@RequiredArgsConstructor
public class AuctionCacheService {

    private final AuctionRepository auctionRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String AUCTION_KEY_PREFIX = "auction:";
    // Redis Hash field constants
    private static final String F_IS_END = "is_end";
    private static final String F_START_TS = "start_ts";
    private static final String F_END_TS = "end_ts";
    private static final String F_CURRENT_PRICE = "current_price";
    private static final String F_BID_UNIT = "bid_unit";
    private static final String F_OWNER_ID = "owner_id";
    private static final String F_BUY_NOW_FLAG = "buy_now_flag";
    private static final String F_BUY_NOW_PRICE = "buy_now_price";

    /**
     * 경매 메타 캐시 보정/워밍
     * <P>
     * 필수 필드가 없으면 DB 스냅샷으로 채운다.
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @RETURN 없음
     */
    public void ensureLoaded(Long auctionId) {
        Auction a = auctionRepository.findById(auctionId).orElse(null);
        if (a == null)
            return;

        String key = AUCTION_KEY_PREFIX + auctionId;
        HashOperations<String, String, String> h = redisTemplate.opsForHash();

        if (Boolean.FALSE.equals(redisTemplate.hasKey(key)) || h.entries(key).isEmpty()
                || h.get(key, F_CURRENT_PRICE) == null || h.get(key, F_BID_UNIT) == null
                || h.get(key, F_START_TS) == null || h.get(key, F_END_TS) == null || h.get(key, F_OWNER_ID) == null
                || h.get(key, F_BUY_NOW_FLAG) == null) {
            String buyNowFlag = a.isBuyNowFlag() ? "1" : "0";
            String buyNowPrice = (a.getBuyNowPrice() != null) ? a.getBuyNowPrice().toPlainString() : "";
            h.putAll(key, Map.of(F_IS_END, a.isEnd() ? "1" : "0", F_START_TS,
                    String.valueOf(a.getStartDatetime().toEpochSecond(ZoneOffset.UTC)), F_END_TS,
                    String.valueOf(a.getEndDatetime().toEpochSecond(ZoneOffset.UTC)), F_CURRENT_PRICE,
                    a.getCurrentPrice().toPlainString(), F_BID_UNIT, a.getBidUnit().toBigDecimal().toPlainString(),
                    F_OWNER_ID, String.valueOf(a.getMember() != null ? a.getMember().getId() : 0L), F_BUY_NOW_FLAG,
                    buyNowFlag, F_BUY_NOW_PRICE, buyNowPrice));
        }
    }

    /**
     * 강제 새로고침: DB 스냅샷으로 Redis 해시 전 필드를 덮어쓴다.
     * <p>
     * 캐시가 존재하지만 오래되어 잘못된 상태(예: end_ts, is_end)가 남아 있을 때 사용.
     * </p>
     */
    public void reloadFromDb(Long auctionId) {
        Auction a = auctionRepository.findById(auctionId).orElse(null);
        if (a == null)
            return;
        String key = AUCTION_KEY_PREFIX + auctionId;
        HashOperations<String, String, String> h = redisTemplate.opsForHash();
        String buyNowFlag = a.isBuyNowFlag() ? "1" : "0";
        String buyNowPrice = (a.getBuyNowPrice() != null) ? a.getBuyNowPrice().toPlainString() : "";
        h.putAll(key,
                Map.of(F_IS_END, a.isEnd() ? "1" : "0", F_START_TS,
                        String.valueOf(a.getStartDatetime().toEpochSecond(ZoneOffset.UTC)), F_END_TS,
                        String.valueOf(a.getEndDatetime().toEpochSecond(ZoneOffset.UTC)), F_CURRENT_PRICE,
                        a.getCurrentPrice().toPlainString(), F_BID_UNIT, a.getBidUnit().toBigDecimal().toPlainString(),
                        F_OWNER_ID, String.valueOf(a.getMember() != null ? a.getMember().getId() : 0L), F_BUY_NOW_FLAG,
                        buyNowFlag, F_BUY_NOW_PRICE, buyNowPrice));
    }

    /**
     * 디버그용: Redis 해시 스냅샷을 반환한다(존재하지 않는 필드는 null).
     */
    public Map<String, String> getSnapshot(Long auctionId) {
        String key = AUCTION_KEY_PREFIX + auctionId;
        HashOperations<String, String, String> h = redisTemplate.opsForHash();
        Map<String, String> snap = new HashMap<>();
        snap.put(F_IS_END, h.get(key, F_IS_END));
        snap.put(F_START_TS, h.get(key, F_START_TS));
        snap.put(F_END_TS, h.get(key, F_END_TS));
        snap.put(F_CURRENT_PRICE, h.get(key, F_CURRENT_PRICE));
        snap.put(F_BID_UNIT, h.get(key, F_BID_UNIT));
        snap.put(F_OWNER_ID, h.get(key, F_OWNER_ID));
        snap.put(F_BUY_NOW_FLAG, h.get(key, F_BUY_NOW_FLAG));
        snap.put(F_BUY_NOW_PRICE, h.get(key, F_BUY_NOW_PRICE));
        return snap;
    }

    /**
     * 현재가 보정
     * <P>
     * 새 값이 더 크면 갱신. 파싱 실패 시 보수적으로 갱신하지 않는다(다운데이트 방지).
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @PARAM currentPriceStr 새 현재가(문자열)
     * @RETURN 없음
     */
    public void overwritePrice(Long auctionId, String currentPriceStr) {
        String key = AUCTION_KEY_PREFIX + auctionId;
        String cur = (String) redisTemplate.opsForHash().get(key, F_CURRENT_PRICE);
        try {
            if (cur == null) {
                redisTemplate.opsForHash().put(key, F_CURRENT_PRICE, currentPriceStr);
                return;
            }
            BigDecimal newV = new BigDecimal(currentPriceStr);
            BigDecimal oldV = new BigDecimal(cur);
            if (newV.compareTo(oldV) > 0) {
                redisTemplate.opsForHash().put(key, F_CURRENT_PRICE, currentPriceStr);
            }
        } catch (Exception ignore) {
            // no-op: 보수적으로 무시 (로그 노이즈 방지)
        }
    }

    /**
     * 강제 동기화: DB의 현재가로 Redis current_price를 덮어쓴다(상향/하향 모두).
     * <p>
     * 컨슈머에서 DB 반영 실패(Dead-letter) 시 보상 동기화에 사용.
     * </p>
     */
    @Transactional(readOnly = true)
    public void syncExactCurrentPrice(Long auctionId) {
        Auction a = auctionRepository.findById(auctionId).orElse(null);
        if (a == null || a.getCurrentPrice() == null)
            return;
        String key = AUCTION_KEY_PREFIX + auctionId;
        try {
            redisTemplate.opsForHash().put(key, F_CURRENT_PRICE, a.getCurrentPrice().toPlainString());
        } catch (Exception ignore) {
            // 보수적으로 무시
        }
    }

    /**
     * 조기 종료/취소 시 Redis is_end=1.
     * <P>
     * 키가 없으면 생성해도 안전. DB 트랜잭션 커밋 직후 호출 권장.
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @RETURN 없음
     */
    public void markEnded(Long auctionId) {
        String key = AUCTION_KEY_PREFIX + auctionId;
        redisTemplate.opsForHash().put(key, F_IS_END, "1");
    }

    /**
     * 관리자 연장/재개 시 Redis end_ts 및 is_end=0 세팅.
     * <P>
     * DB에서 end_ts 변경 후 호출.
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @PARAM newEndTsEpoch 새 종료시각(초)
     * @RETURN 없음
     */
    public void reopenUntil(Long auctionId, long newEndTsEpoch) {
        String key = AUCTION_KEY_PREFIX + auctionId;
        redisTemplate.opsForHash().putAll(key, Map.of(F_END_TS, String.valueOf(newEndTsEpoch), F_IS_END, "0"));
    }

    /**
     * 관리자 메타 변경을 한 번에 반영(옵션).
     * <P>
     * 필요 시 is_end/end_ts 동시 갱신.
     * </P>
     * 
     * @PARAM auctionId 경매 ID
     * @PARAM isEnd 종료 여부
     * @PARAM endTsEpoch 종료 시각(초)
     * @RETURN 없음
     */
    public void applyAdminChange(Long auctionId, boolean isEnd, long endTsEpoch) {
        String key = AUCTION_KEY_PREFIX + auctionId;
        redisTemplate.opsForHash().putAll(key,
                Map.of(F_IS_END, isEnd ? "1" : "0", F_END_TS, String.valueOf(endTsEpoch)));
    }
}
