package com.bukadong.tcg.global.mail.provider;

import com.bukadong.tcg.global.mail.dto.MailContext;
import com.bukadong.tcg.global.mail.dto.MailType;

import java.util.Map;

public interface MailValueProvider {

    MailType getMailType();

    Map<String, Object> createValues(MailContext context);
}
