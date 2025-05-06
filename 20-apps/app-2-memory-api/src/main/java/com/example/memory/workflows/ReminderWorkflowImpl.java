package com.example.memory.workflows;

import com.example.memory.activities.ReminderActivity;
import io.temporal.activity.ActivityOptions;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;

public class ReminderWorkflowImpl implements ReminderWorkflow {
    private static final Logger log = LoggerFactory.getLogger(ReminderWorkflowImpl.class);
    private final ReminderActivity reminderActivity =
            Workflow.newActivityStub(ReminderActivity.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(Duration.ofSeconds(30))
                            .build());

    @Override
    public void checkAndSendReminders() {
        // This workflow will run indefinitely on a cron schedule
        log.info("Reminder workflow started at  {}", LocalDateTime.now());
        reminderActivity.checkAndSendReminders();
    }
}