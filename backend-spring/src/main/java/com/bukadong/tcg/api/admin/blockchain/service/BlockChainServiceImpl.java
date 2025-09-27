package com.bukadong.tcg.api.admin.blockchain.service;

import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;

import java.io.IOException;
import java.math.BigInteger;

/**
 * 블록체인 헬스체크 서비스
 */
@Service
@RequiredArgsConstructor
public class BlockChainServiceImpl implements BlockChainService {

    private static final Logger log = LoggerFactory.getLogger(BlockChainServiceImpl.class);

    private final Web3j web3j;

    /**
     * 연결된 블록체인 네트워크의 최신 블록 번호를 조회
     */
    @Override
    public BigInteger getLatestBlockNumber() {
        try {
            return web3j.ethBlockNumber().send().getBlockNumber();
        } catch (IOException e) {
            log.error("블록체인 네트워크에서 최신 블록 번호를 가져오는 데 실패했습니다.");
            throw new BaseException(BaseResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
