package com.bukadong.tcg.global.blockchain.util;

import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 스마트 컨트랙트 트랜잭션 실패 시 반환되는 revertReason(16진수)을
 * Custom Error 이름으로 변환해주는 유틸리티 클래스
 * <p>
 * web3j 4.13.0 에선 제공되지 않는 "CustomError" 데이터 타입을 직접 처리하기 위해 구현됨
 */
public class ContractErrorDecoder {

    /**
     * revertReason(16진수)을 받아서 해당하는 ContractError Enum을 반환
     */
    public static Optional<ContractError> decode(String revertReason) {
        if (StringUtils.hasText(revertReason) || !revertReason.startsWith("0x") || revertReason.length() < 10) {
            return Optional.empty();
        }
        return ContractError.getBySelector(revertReason.substring(0, 10));
    }
}
