package com.example.adapter.workflows;

import com.example.adapter.activities.WebhookDynamoDbActivity;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;

public class WebhookWorkflowImpl implements WebhookWorkflow {
    private final Logger logger = Workflow.getLogger(WebhookWorkflowImpl.class);

    private final WebhookDynamoDbActivity dynamoDbActivity =
            Workflow.newActivityStub(WebhookDynamoDbActivity.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(Duration.ofSeconds(10))
                            .build());

    @Override
    public void processWebhook(String payload) {
        logger.info("Processing webhook in workflow: {}", payload);

        // Save webhook to DynamoDB
        dynamoDbActivity.saveWebhookToDynamoDb(payload, Workflow.getInfo().getWorkflowId());

        // Add other business logic here
        // You can call other activities to perform various tasks

        logger.info("webhook processing completed");
    }
}