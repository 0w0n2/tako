package com.bukadong.tcg.global.mail.dto;

import java.util.Map;

public interface MailValueProvider {

    MailType getMailType();

    Map<String, Object> createValues(MailContext context);
}
