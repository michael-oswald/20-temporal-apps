package com.example.memory.service;

import com.example.memory.controller.MemoryController;
import com.example.memory.workflows.EmailCampaignWorkflow;
import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Service
public class MemoryService {
    private static final Logger logger = LoggerFactory.getLogger(MemoryService.class);

    private final WorkflowClient workflowClient;
    private final String taskQueue;
    private final DynamoDbClient dynamoDbClient;

    @Autowired
    public MemoryService(WorkflowClient workflowClient,
                         @Value("${temporal.taskqueue:BatchEmailTaskQueue}") String taskQueue,
                         DynamoDbClient dynamoDbClient) {
        this.workflowClient = workflowClient;
        this.taskQueue = taskQueue;
        this.dynamoDbClient = dynamoDbClient;
    }

    public void startWorkflow(String workflowId, MemoryController.BatchEmailSendRequest payload) {
        EmailCampaignWorkflow workflow = workflowClient.newWorkflowStub(
                EmailCampaignWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(taskQueue)
                        .setWorkflowId(workflowId)
                        .setWorkflowIdReusePolicy(WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_REJECT_DUPLICATE)
                        .build());

        // Asynchronously start the workflow and continue immediately
        WorkflowClient.start(workflow::startEmailCampaign, payload.emails);
        logger.info("Started memory workflow with ID: {}", workflowId);
    }
}