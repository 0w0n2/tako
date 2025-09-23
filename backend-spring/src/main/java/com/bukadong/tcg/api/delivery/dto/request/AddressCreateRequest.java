package com.bukadong.tcg.api.delivery.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressCreateRequest {
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
