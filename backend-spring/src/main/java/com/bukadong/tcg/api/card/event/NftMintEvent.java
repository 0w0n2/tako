package com.bukadong.tcg.api.card.event;

/**
 * NFT 발행을 비동기로 처리하기 위한 이벤트 객체
 *
 * @param physicalCardId DB에 저장된 PhysicalCard의 ID
 * @param tokenId        블록체인에 발행할 토큰 ID
 * @param secret         사용자 클레임에 사용할 원본 시크릿 코드
 */
public record NftMintEvent(
        Long physicalCardId,
        long tokenId,
        String secret
) {
}
