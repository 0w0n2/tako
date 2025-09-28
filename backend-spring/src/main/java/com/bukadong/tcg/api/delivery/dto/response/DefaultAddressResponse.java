package com.bukadong.tcg.api.delivery.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DefaultAddressResponse {
    Long id;
    String placeName;
    String baseAddress;
    String zipcode;
}
