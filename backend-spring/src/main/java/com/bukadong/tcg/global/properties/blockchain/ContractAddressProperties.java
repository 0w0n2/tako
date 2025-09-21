package com.bukadong.tcg.global.properties.blockchain;

import jakarta.validation.constraints.NotBlank;

public record ContractAddressProperties(
        AuctionProperties auction,

        @NotBlank
        String takoCardNft
) {
}
