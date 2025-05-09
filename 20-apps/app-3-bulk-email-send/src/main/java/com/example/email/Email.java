package com.example.email;

public class Email {
    private String emailId;
    private String to;
    private String body;
    private String subject;

    public Email (){}
    public Email(String emailId, String to, String body, String subject) {
        this.emailId = emailId;
        this.to = to;
        this.body = body;
        this.subject = subject;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
