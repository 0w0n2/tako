package com.bukadong.tcg.api.test;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class RequestDto {

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;
}
