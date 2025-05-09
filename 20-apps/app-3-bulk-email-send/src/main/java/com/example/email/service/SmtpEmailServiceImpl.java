package com.example.email.service;

import jakarta.annotation.PostConstruct;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class SmtpEmailServiceImpl implements EmailService {
    private Session session;
    private static final Logger logger = LoggerFactory.getLogger(SmtpEmailServiceImpl.class);
    @Value("${email.from:noreply@example.com}")
    private String defaultFromEmail;

    @PostConstruct
    public void init() {
        this.session = createMailSession();
    }

    private Session createMailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "localhost");
        props.put("mail.smtp.port", "1025"); // MailHog default
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");

        return Session.getInstance(props);
    }

    @Override
    public void send(String toEmail, String bodyText) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(defaultFromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Test Email from SmtpEmailService");
            message.setText(bodyText);

            Transport.send(message);
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send email via SMTP", e);
        }
    }
}
