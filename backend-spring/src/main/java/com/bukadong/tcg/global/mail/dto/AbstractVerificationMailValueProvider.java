package com.bukadong.tcg.global.mail.dto;

import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import com.bukadong.tcg.global.properties.WebUrlProperties;
import lombok.RequiredArgsConstructor;

import static com.bukadong.tcg.global.constant.MailConstants.*;

import java.util.Map;

@RequiredArgsConstructor
public abstract class AbstractVerificationMailValueProvider implements MailValueProvider {

    private final WebUrlProperties webUrlProperties;

    @Override
    public Map<String, Object> createValues(MailContext context) {
        VerificationCode code = context.getVerificationCode()
                .orElseThrow(() -> new BaseException(BaseResponseStatus.MAIL_SEND_FAIL));

        return Map.of(
                MAIN_URL_KEY, webUrlProperties.main(),
                LOGO_IMAGE_URL_KEY, webUrlProperties.logo(),
                CODE_KEY, code.code()
        );
    }
}