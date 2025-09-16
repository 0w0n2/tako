package com.bukadong.tcg.global.mail.service;

import com.bukadong.tcg.global.common.base.BaseResponseStatus;
import com.bukadong.tcg.global.common.exception.BaseException;
import com.bukadong.tcg.global.mail.dto.MailContext;
import com.bukadong.tcg.global.mail.dto.MailType;
import com.bukadong.tcg.global.mail.dto.MailValueProvider;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MailSendService {

    private final JavaMailSender javaMailSender;
    private final Map<MailType, MailValueProvider> providerMap;

    public MailSendService(JavaMailSender javaMailSender, List<MailValueProvider> providers) {
        this.javaMailSender = javaMailSender;
        this.providerMap = providers.stream()
                .collect(Collectors.toMap(MailValueProvider::getMailType, Function.identity()));
    }

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

    public void sendMail(String to, MailType mailType, MailContext context) {
        MailValueProvider provider = findProvider(mailType);
        Map<String, Object> values = provider.createValues(context);
        sendHtmlMail(to, mailType, values);
    }

    public void sendHtmlMail(String to, MailType mailType, Map<String, Object> values) {
        String subject = mailType.getSubject();
        String content = mailType.buildContent(values);
        sendHtmlMailInternal(to, subject, content);
    }

    public void sendHtmlMailInternal(String to, String subject, String content) {
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

    private MailValueProvider findProvider(MailType mailType) {
        MailValueProvider provider = providerMap.get(mailType);
        if (provider == null) {
            throw new BaseException(BaseResponseStatus.MAIL_UNSUPPORTED_TYPE);
        }
        return provider;
    }
}
