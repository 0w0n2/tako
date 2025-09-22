package com.bukadong.tcg.api.media.entity;

/** 미디어 소유 리소스 타입 */
public enum MediaType {
    MEMBER_PROFILE, // 회원 프로필
    MEMBER_BACKGROUND,  // 회원 배경화면
    NOTICE_ATTACHMENT, // 공지사항 첨부파일
    CARD, // 카드
    CATEGORY_MAJOR, // 대분류
    CATEGORY_MEDIUM, // 중분류
    AUCTION_ITEM, // 경매 아이템
    AUCTION_AI, // 경매 AI
    AUCTION_REVIEW, // 경매 리뷰
    NOTICE, // 공지사항
    INQUIRY // 문의
}
