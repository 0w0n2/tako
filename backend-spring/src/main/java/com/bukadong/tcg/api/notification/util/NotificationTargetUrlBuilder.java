package com.bukadong.tcg.api.notification.util;

import org.springframework.stereotype.Component;

/**
 * 알림 타겟 URL 빌더
 * <P>
 * 현재는 정책상 빈 문자열을 반환한다. 추후 라우팅 규칙 도입 시 이 컴포넌트만 수정하면 된다.
 * </P>
 * 
 * @PARAM 없음
 * @RETURN 없음
 */
@Component
public class NotificationTargetUrlBuilder {

    /**
     * 경매 상세로 연결 등 추후 규칙을 반영할 자리.
     * <P>
     * 지금은 항상 빈 문자열을 반환한다.
     * </P>
     * 
     * @PARAM causeId 원인 리소스 ID
     * @RETURN 빈 문자열
     */
    public String buildForAuction(Long causeId) {
        return "";
    }

    /**
     * 카드 상세용 (현재 빈 문자열)
     * 
     * @PARAM causeId 카드 ID
     * @RETURN 빈 문자열
     */
    public String buildForCard(Long causeId) {
        return "";
    }

    /**
     * 문의 상세용 (현재 빈 문자열)
     * 
     * @PARAM causeId 문의 ID
     * @RETURN 빈 문자열
     */
    public String buildForInquiry(Long causeId) {
        return "";
    }
}
