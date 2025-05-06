package com.example.memory.activities;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ReminderActivity {
    @ActivityMethod
    void checkAndSendReminders();
}