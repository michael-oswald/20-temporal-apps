package com.example.adapter.workflows;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface WebhookWorkflow {
    @WorkflowMethod
    void processWebhook(String payload);
}
