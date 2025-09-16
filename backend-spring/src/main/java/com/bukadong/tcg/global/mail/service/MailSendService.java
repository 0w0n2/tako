package com.bukadong.tcg.global.mail.service;

import com.bukadong.tcg.global.mail.dto.MailContext;
import com.bukadong.tcg.global.mail.dto.MailType;

import java.util.Map;

public interface MailSendService {
    void sendSimpleMail(String to, String subject, String content);

    void sendMail(String to, MailType mailType, MailContext context);

    void sendHtmlMail(String to, MailType mailType, Map<String, Object> values);
}
