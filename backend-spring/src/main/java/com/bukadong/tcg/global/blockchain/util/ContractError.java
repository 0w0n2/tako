package com.bukadong.tcg.global.blockchain.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum ContractError {

    /* --- AuctionEscrow Errors --- */
    INCORRECT_AMOUNT("0x69640e72"),
    INVALID_INITIALIZATION("0xf92ee8a9"),
    INVALID_STATE("0xbaf3f0f7"),
    NOT_BUYER("0x472e017e"),
    NOT_INITIALIZING("0xd7e6bcf8"),
    NOT_SELLER("0x5ec82351"),
    REENTRANCY_GUARD_REENTRANT_CALL("0x3ee5aeb5"),
    TRANSFER_FAILED("0x90b8ec18"),

    /* --- AuctionFactory Errors --- */
    ADDRESS_EMPTY_CODE("0x9996b315"),
    ERC1967_INVALID_IMPLEMENTATION("0x4c9c8ce3"),
    ERC1967_NON_PAYABLE("0xb398979f"),
    FAILED_CALL("0xd6bda275"),
    FAILED_DEPLOYMENT("0xb06ebf3d"),
    INSUFFICIENT_BALANCE("0xcf479181"),
    OWNABLE_INVALID_OWNER("0x1e4fbdf7"),
    OWNABLE_UNAUTHORIZED_ACCOUNT("0x118cdaa7"),
    UUPS_UNAUTHORIZED_CALL_CONTEXT("0xe07c8dba"),
    UUPS_UNSUPPORTED_PROXIABLE_UUID("0xaa1d49a4"),

    /* --- TakoCardNFT Errors --- */
    ERC721_INCORRECT_OWNER("0x64283d7b"),
    ERC721_INSUFFICIENT_APPROVAL("0x177e802f"),
    ERC721_INVALID_APPROVER("0xa9fbf51f"),
    ERC721_INVALID_OPERATOR("0x5b08ba18"),
    ERC721_INVALID_OWNER("0x89c62b64"),
    ERC721_INVALID_RECEIVER("0x64a0ae92"),
    ERC721_INVALID_SENDER("0x73c6ac6e"),
    ERC721_NONEXISTENT_TOKEN("0x7e273289");

    private final String selector;

    /* selector 을 key 로 사용하여 Enum 상수를 조회하기 위한 static Map */
    private static final Map<String, ContractError> SELECTOR_MAP = Stream.of(values())
            .collect(Collectors.toUnmodifiableMap(ContractError::getSelector, Function.identity()));

    /* 16진수 selector 문자열로부터 해당하는 ContractError Enum 상수를 조회 */
    public static Optional<ContractError> getBySelector(String selector) {
        return Optional.ofNullable(SELECTOR_MAP.get(selector));
    }
}
