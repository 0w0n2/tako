package com.bukadong.tcg.global.blockchain.util;

import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;

import java.util.Optional;

@Slf4j
@Component
public class ContractExceptionHelper {

    public BaseException handleTransactionException(TransactionException e) {
        String revertReason = e.getTransactionReceipt()
                .map(TransactionReceipt::getRevertReason)
                .orElse("No revert reason found");

        Optional<ContractError> errorOpt = ContractErrorDecoder.decode(revertReason);
        if (errorOpt.isPresent()) {
            ContractError error = errorOpt.get();
            log.error("Smart Contract is failed. --- Decoded Error: {}", error.name());

            BaseResponseStatus status = switch (error) {
                case OWNABLE_UNAUTHORIZED_ACCOUNT, NOT_SELLER, NOT_BUYER -> BaseResponseStatus.CONTRACT_UNAUTHORIZED;
                case INVALID_STATE -> BaseResponseStatus.CONTRACT_INVALID_STATE;
                case INCORRECT_AMOUNT -> BaseResponseStatus.CONTRACT_INCORRECT_AMOUNT;
                // TODO: 모든 커스텀 에러들 여기에 추가
                default -> BaseResponseStatus.CONTRACT_UNKNOWN;
            };
            return new BaseException(status);
        } else {
            log.error("Unknown error during smart contract execution. --- Raw Revert Reason: [{}]", revertReason);
            throw new BaseException(BaseResponseStatus.CONTRACT_UNKNOWN);
        }
    }

    /**
     * TransactionException을 분석하여 에러 로그를 출력
     */
    public void logTransactionException(TransactionException e, String context) {
        String revertReason = e.getTransactionReceipt()
                .map(TransactionReceipt::getRevertReason)
                .orElse("No revert reason found");

        Optional<ContractError> errorOpt = ContractErrorDecoder.decode(revertReason);
        if (errorOpt.isPresent()) {
            ContractError error = errorOpt.get();
            log.error("스마트 컨트랙트 트랜잭션 실패. Context: [{}], Decoded Error: [{}]", context, error.name());
        } else {
            log.error("알 수 없는 컨트랙트 에러 발생. Context: [{}], Raw Revert Reason: [{}]", context, revertReason);
        }
    }
}
