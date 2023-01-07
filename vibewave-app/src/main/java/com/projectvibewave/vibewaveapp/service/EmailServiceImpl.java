package com.projectvibewave.vibewaveapp.service;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Map;

@Service
@AllArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final static String APP_EMAIL = "vibewave@no-reply.com";
    private final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    @Async
    public void send(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setText(content, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(APP_EMAIL);
            mailSender.send(message);
        } catch (MessagingException e) {
            logger.error("Failed to send an email", e);
            throw new IllegalStateException("Failed to send an email");
        }
    }

    @Override
    @Async
    public void sendHtml(String to, String subject, Map<String, Object> templateModel, String emailTemplateName) {
        var context = new Context();
        context.setVariables(templateModel);
        String htmlContent = templateEngine.process("emails/" + emailTemplateName, context);
        send(to, subject, htmlContent);
    }
}
