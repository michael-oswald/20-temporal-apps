package com.example.adapter.service;

import com.example.adapter.workflows.WebhookWorkflow;
import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WebhookService {
    private static final Logger logger = LoggerFactory.getLogger(WebhookService.class);

    private final WorkflowClient workflowClient;
    private final String taskQueue;

    @Autowired
    public WebhookService(WorkflowClient workflowClient,
                          @Value("${temporal.taskqueue:WebhookTaskQueue}") String taskQueue) {
        this.workflowClient = workflowClient;
        this.taskQueue = taskQueue;
    }

    public void startWebhookWorkflow(String workflowId, String payload) {
        WebhookWorkflow workflow = workflowClient.newWorkflowStub(
                WebhookWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(taskQueue)
                        .setWorkflowId(workflowId)
                        .setWorkflowIdReusePolicy(WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_REJECT_DUPLICATE)
                        .build());

        // Asynchronously start the workflow and continue immediately
        WorkflowClient.start(workflow::processWebhook, payload);
        logger.info("Started webhook workflow with ID: {}", workflowId);
    }
}