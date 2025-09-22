package com.bukadong.tcg.api.card.dto.response;

import com.bukadong.tcg.global.blockchain.contracts.TakoCardNFT;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Builder
@Schema(description = "NFT 카드의 경매 이력 응답 DTO")
public record NftAuctionHistoryResponseDto(
        @Schema(description = "판매자 지갑 주소")
        String seller,

        @Schema(description = "구매자 지갑 주소")
        String buyer,

        @Schema(description = "낙찰 가격 (ETH 단위)", example = "1.5")
        String priceInEth,

        @Schema(description = "카드 등급 ID")
        BigInteger gradeId,

        @Schema(description = "거래 시각")
        LocalDateTime timestamp
) {
    public static NftAuctionHistoryResponseDto toDto(TakoCardNFT.AuctionHistory history) {
        BigDecimal priceInWei = new BigDecimal(history.price);
        BigDecimal priceInEthDecimal = Convert.fromWei(priceInWei, Convert.Unit.ETHER);

        return NftAuctionHistoryResponseDto.builder()
                .seller(history.seller)
                .buyer(history.buyer)
                .priceInEth(priceInEthDecimal.toPlainString())
                .gradeId(history.gradeId)
                .timestamp(
                        LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(history.timestamp.longValue()),
                                ZoneId.systemDefault()
                        )
                )
                .build();
    }
}
