package com.example.adapter.activities;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface WebhookDynamoDbActivity {
    @ActivityMethod
    void saveWebhookToDynamoDb(String payload, String workflowId);
}