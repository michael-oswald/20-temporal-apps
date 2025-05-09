package com.example.email.workflows;

import com.example.email.Email;
import com.example.email.activities.EmailActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.List;

public class EmailBatchWorkflowImpl implements EmailBatchWorkflow {

    private final EmailActivities activities = Workflow.newActivityStub(
            EmailActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(1))
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
