package com.example.email.activities;

import com.example.email.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailActivitiesImpl implements EmailActivities {

    private final EmailService emailService;
    private static final Logger logger = LoggerFactory.getLogger(EmailActivitiesImpl.class);

    public EmailActivitiesImpl(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void sendEmail(String emailId, String to, String body) {
        try {
            emailService.send(to, body);
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw e; // Let Temporal retry
        }
    }
}

