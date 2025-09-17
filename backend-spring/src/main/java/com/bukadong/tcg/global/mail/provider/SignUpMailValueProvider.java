package com.bukadong.tcg.global.mail.provider;

import com.bukadong.tcg.global.mail.dto.MailType;
import com.bukadong.tcg.global.properties.WebUrlProperties;
import org.springframework.stereotype.Component;

@Component
public class SignUpMailValueProvider extends AbstractVerificationMailValueProvider {

    @Override
    public MailType getMailType() {
        return MailType.SIGN_UP;
    }

    public SignUpMailValueProvider(WebUrlProperties webUrlProperties) {
        super(webUrlProperties);
    }
}
