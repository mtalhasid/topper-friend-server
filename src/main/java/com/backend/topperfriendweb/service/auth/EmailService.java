package com.backend.topperfriendweb.service.auth;

import org.springframework.stereotype.Service;

/**
 * Thin wrapper around MailjetService to decouple AuthService from a concrete email provider.
 */
@Service
public class EmailService {
    private final MailjetService mailjetService;

    public EmailService(MailjetService mailjetService) {
        this.mailjetService = mailjetService;
    }

    public void sendVerificationEmail(String email, String name, String otp) throws Exception {
        mailjetService.sendVerificationEmail(email, name, otp);
    }
}
