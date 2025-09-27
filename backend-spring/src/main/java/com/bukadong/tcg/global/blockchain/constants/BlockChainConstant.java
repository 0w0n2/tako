package com.bukadong.tcg.global.blockchain.constants;

import java.math.BigInteger;

public final class BlockChainConstant {
    public BlockChainConstant() {
    }

    /* Alchemy의 동시 무료 블록 조회 가능 횟수 */
    public static final BigInteger BLOCK_RANGE_LIMIT = BigInteger.valueOf(9);
}
