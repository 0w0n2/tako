package com.bukadong.tcg.api.card.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PhysicalCardStatus {
    PENDING("아직 발행되지 않은 NFT입니다."),                      // DB 에만 등록되고 아직 NFT로 발행되지 않은 상태 (컨트랙트는 비동기로 처리)
    MINTED("클레임 가능한 NFT입니다."),                           // NFT 발행과 시크릿 등록까지 완료되어 사용자의 클레임 대기 중인 상태
    CLAIMED("이미 다른 사용자가 클레임한 NFT입니다."),              // 특정 사용자가 성공적으로 클레임하여 소유권을 가져간 상태
    FAILED("발행에 실패한 NFT입니다. 관리자에게 문의해주세요.");      // 온체인 발행 또는 등록 실패

    private final String message;
}
