package com.bukadong.tcg.api.card.dto.response;

import com.bukadong.tcg.api.card.entity.CardAiGrade;
import com.bukadong.tcg.api.member.entity.Member;
import com.bukadong.tcg.global.blockchain.contracts.TakoCardNFT;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Builder
@Schema(description = "NFT 카드의 경매 이력 응답 DTO")
public record NftAuctionHistoryResponseDto(

        @Schema(description = "판매자 정보")
        MemberInfo seller,

        @Schema(description = "구매자 정보")
        MemberInfo buyer,

        @Schema(description = "낙찰 가격 (ETH 단위)", example = "1.5")
        String priceInEth,

        @Schema(description = "AI 등급 정보")
        GradeInfo grade,

        @Schema(description = "거래 시각")
        LocalDateTime timestamp
) {
    public static NftAuctionHistoryResponseDto toDto(TakoCardNFT.AuctionHistory history, Member seller, Member buyer, CardAiGrade cardAiGrade) {
        BigDecimal priceInWei = new BigDecimal(history.price);
        BigDecimal priceInEthDecimal = Convert.fromWei(priceInWei, Convert.Unit.ETHER);

        return NftAuctionHistoryResponseDto.builder()
                .seller(MemberInfo.toDto(seller, history.seller))
                .buyer(MemberInfo.toDto(buyer, history.buyer))
                .priceInEth(priceInEthDecimal.toPlainString())
                .grade(GradeInfo.toDto(history.gradeId, cardAiGrade))
                .timestamp(
                        LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(history.timestamp.longValue()),
                                ZoneId.systemDefault()
                        )
                )
                .build();
    }
}
