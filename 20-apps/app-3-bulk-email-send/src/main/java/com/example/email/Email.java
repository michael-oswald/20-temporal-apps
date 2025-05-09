package com.example.email;

public class Email {
    private String emailId;
    private String to;
    private String body;
    private String subject;

    public Email(String emailId, String to, String body, String subject) {
        this.emailId = emailId;
        this.to = to;
        this.body = body;
        this.subject = subject;
    }

    public String getEmailId() {
        return emailId;
    }

    public String getTo() {
        return to;
    }

    public String getBody() {
        return body;
    }

    public String getSubject() {
        return subject;
    }
}
