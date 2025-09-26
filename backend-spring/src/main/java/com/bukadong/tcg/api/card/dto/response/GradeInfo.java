package com.bukadong.tcg.api.card.dto.response;

import com.bukadong.tcg.api.card.entity.CardAiGrade;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigInteger;

@Builder
public record GradeInfo(
        @Schema(description = "카드 등급 ID")
        BigInteger gradeId,
        @Schema(description = "AI 컨디션 등급")
        String gradeCode
) {
    public static GradeInfo toDto(BigInteger gradeId, CardAiGrade cardAiGrade) {
        return GradeInfo.builder()
                .gradeId(gradeId)
                .gradeCode(cardAiGrade == null ? null : cardAiGrade.getGradeCode())
                .build();
    }
}