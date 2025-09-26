package com.bukadong.tcg.api.trust.service;

import com.bukadong.tcg.api.auction.entity.DescriptionMatch;

public interface MemberTrustService {

    int getScore(Long memberId);

    void updateOnReview(Long targetMemberId, DescriptionMatch descriptionMatch, int star);
}
