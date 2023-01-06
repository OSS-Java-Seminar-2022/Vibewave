package com.projectvibewave.vibewaveapp.service;

import org.checkerframework.checker.units.qual.A;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;

public interface EmailService {
    String ACCOUNT_CONFIRMATION_TEMPLATE = "email-confirmation";
    String VERIFICATION_REQUEST_UPDATE_TEMPLATE = "verification-request-update";

    @Async
    void send(String to, String subject, String content);

    @Async
    void sendHtml(String to, String subject, Map<String, Object> templateModel, String emailTemplateFile);
}
