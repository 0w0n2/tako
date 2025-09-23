package com.bukadong.tcg.api.delivery.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AddressSummaryResponse {
    Long id;
    String placeName;
    String baseAddress;
    String zipcode;
    boolean isDefault;
}
