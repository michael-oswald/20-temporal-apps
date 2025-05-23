package com.example.email.activities;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface EmailActivity {
    @ActivityMethod
    void sendEmail(String emailId, String to, String body);
}