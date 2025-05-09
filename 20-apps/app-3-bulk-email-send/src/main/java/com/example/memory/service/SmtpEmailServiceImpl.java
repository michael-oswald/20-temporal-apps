package com.example.memory.service;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class SmtpEmailServiceImpl implements EmailService {
    private final Session session;
    private final String fromEmail;
    private static final Logger logger = LoggerFactory.getLogger(SmtpEmailServiceImpl.class);

    public SmtpEmailServiceImpl(String fromEmail) {
        this.fromEmail = fromEmail;
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
            message.setFrom(new InternetAddress(fromEmail));
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
