package com.bukadong.tcg.global.mail.service;

import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import com.bukadong.tcg.global.mail.dto.MailType;
import com.bukadong.tcg.global.util.RedisUtils;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MailSendService {

    private final JavaMailSender javaMailSender;

    public void sendSimpleMail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();

        try {
            message.setTo(to);              // 수신자
            message.setSubject(subject);    // 제목
            message.setText(content);       // 내용

            javaMailSender.send(message);
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.MAIL_SEND_FAIL);
        }
    }

    public void sendHtmlMail(String to, MailType mailType, Map<String, Object> values) {
        String subject = mailType.getSubject();
        String content = mailType.buildContent(values);
        sendHtmlMail(to, subject, content);
    }

    public void sendHtmlMail(String to, String subject, String content) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            helper.setTo(to);               // 수신자
            helper.setSubject(subject);     // 제목
            helper.setText(content, true);  // 내용

            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.MAIL_SEND_FAIL);
        }
    }
}
