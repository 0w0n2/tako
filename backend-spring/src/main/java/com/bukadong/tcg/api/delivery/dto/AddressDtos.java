package com.bukadong.tcg.api.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

public class AddressDtos {

    private AddressDtos() {
    }

    @Value
    @Builder
    public static class CreateRequest {
        @Size(max = 30)
        String placeName; // nullable 허용
        @NotBlank
        @Size(max = 50)
        String name;
        @NotBlank
        @Size(max = 20)
        String phone;
        @NotBlank
        @Size(max = 200)
        String baseAddress;
        @NotBlank
        @Size(max = 150)
        String addressDetail;
        @NotBlank
        @Size(max = 10)
        String zipcode;
        boolean setAsDefault;
    }

    @Value
    @Builder
    public static class UpdateRequest {
        @Size(max = 30)
        String placeName; // nullable 허용
        @NotBlank
        @Size(max = 50)
        String name;
        @NotBlank
        @Size(max = 20)
        String phone;
        @NotBlank
        @Size(max = 200)
        String baseAddress;
        @NotBlank
        @Size(max = 150)
        String addressDetail;
        @NotBlank
        @Size(max = 10)
        String zipcode;
        boolean setAsDefault;
    }

    @Value
    @Builder
    public static class SummaryResponse {
        Long id;
        String placeName;
        String baseAddress;
        String zipcode;
        boolean isDefault;
    }

    @Value
    @Builder
    public static class DetailResponse {
        Long id;
        String placeName;
        String name;
        String phone;
        String baseAddress;
        String addressDetail;
        String zipcode;
        boolean isDefault;
    }

    @Value
    @Builder
    public static class DefaultAddressResponse {
        Long id;
        String placeName;
        String baseAddress;
        String zipcode;
    }
}
