package com.bukadong.tcg.global.mail.provider;

import com.bukadong.tcg.global.mail.dto.MailType;
import com.bukadong.tcg.global.properties.WebUrlProperties;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetMailValueProvider extends AbstractVerificationMailValueProvider {

    @Override
    public MailType getMailType() {
        return MailType.PASSWORD_RESET;
    }

    public PasswordResetMailValueProvider(WebUrlProperties webUrlProperties) {
        super(webUrlProperties);
    }
}
