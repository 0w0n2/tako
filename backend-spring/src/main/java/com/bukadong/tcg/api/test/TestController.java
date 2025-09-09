package com.bukadong.tcg.api.test;

import com.bukadong.tcg.global.common.base.BaseResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/test")
public class TestController {

    @PutMapping()
    public BaseResponse<?> test(@Valid RequestDto requestDto) {
        return new BaseResponse<>();
    }
}
