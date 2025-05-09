package com.example.memory.workflows;

import com.example.memory.Email;
import com.example.memory.activities.EmailActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.List;

public class EmailBatchWorkflowImpl implements EmailBatchWorkflow {

    private final EmailActivities activities = Workflow.newActivityStub(
            EmailActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(1))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setMaximumAttempts(3)
                            .build())
                    .build()
    );

    @Override
    public void sendEmails(List<Email> emails) {
        for (Email email : emails) {
            var emailId = String.format("%s-%s-%d",
                            Workflow.getInfo().getWorkflowId(),
                            email.getTo(),
                            emails.indexOf(email));

            activities.sendEmail(emailId, email.getTo(), email.getBody());
        }
    }
}
