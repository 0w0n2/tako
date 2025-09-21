package com.bukadong.tcg.api.card.entity;

public enum PhysicalCardStatus {
    PENDING,    // DB 에만 등록되고 아직 NFT로 발행되지 않은 상태 (컨트랙트는 비동기로 처리)
    MINTED,     // NFT 발행과 시크릿 등록까지 완료되어 사용자의 클레임 대기 중인 상태
    CLAIMED,    // 특정 사용자가 성공적으로 클레임하여 소유권을 가져간 상태
    FAILED      // 온체인 발행 또는 등록 실패
}
