package com.bukadong.tcg.api.delivery.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AddressDetailResponse {
    Long id;
    String placeName;
    String name;
    String phone;
    String baseAddress;
    String addressDetail;
    String zipcode;
    boolean isDefault;

}