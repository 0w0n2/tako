package com.bukadong.tcg.api.auction.util;

/**
 * 경매 Redis 키 상수
 * <P>
 * 데드라인 ZSET 등 경매 관련 Redis 키 네임스페이스를 한 곳에서 관리한다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
public final class AuctionRedisKeys {
    private AuctionRedisKeys() {
    }

    /** 마감 스케줄용 ZSET: score=endAt(EpochMillis), member=auctionId */
    public static final String DEADLINES_ZSET = "AUCTION:DEADLINES";

    /** 경매 메타 해시 (이미 캐싱 중인 구조와 호환되게 prefix만 제공) */
    public static final String AUCTION_HASH_PREFIX = "auction:"; // 예: auction:{id}
}
