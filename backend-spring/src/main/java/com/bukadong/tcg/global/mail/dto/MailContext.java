package com.bukadong.tcg.global.mail.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.bukadong.tcg.global.mail.constants.MailConstants.*;

public class MailContext {

    private final Map<String, Object> context = new HashMap<>();

    public MailContext withVerificationCode(VerificationCode code) {
        context.put(CODE_KEY, code);
        return this;
    }

    public Optional<VerificationCode> getVerificationCode() {
        return Optional.ofNullable((VerificationCode) context.get(CODE_KEY));
    }
}
