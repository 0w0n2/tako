package com.bukadong.tcg.api.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;

public class AddressDtos {

    private AddressDtos() {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @Size(max = 30)
        private String placeName; // nullable 허용
        @NotBlank
        @Size(max = 50)
        private String name;
        @NotBlank
        @Size(max = 20)
        private String phone;
        @NotBlank
        @Size(max = 200)
        private String baseAddress;
        @NotBlank
        @Size(max = 150)
        private String addressDetail;
        @NotBlank
        @Size(max = 10)
        private String zipcode;
        private boolean setAsDefault;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        @Size(max = 30)
        private String placeName; // nullable 허용
        @NotBlank
        @Size(max = 50)
        private String name;
        @NotBlank
        @Size(max = 20)
        private String phone;
        @NotBlank
        @Size(max = 200)
        private String baseAddress;
        @NotBlank
        @Size(max = 150)
        private String addressDetail;
        @NotBlank
        @Size(max = 10)
        private String zipcode;
        private boolean setAsDefault;
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
