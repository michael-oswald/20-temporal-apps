package com.example.email.service;

import com.example.email.controller.BatchEmailController;
import com.example.email.workflows.EmailCampaignWorkflow;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionRequest;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BatchEmailService {
    private static final Logger logger = LoggerFactory.getLogger(BatchEmailService.class);

    private final WorkflowClient workflowClient;
    private final String taskQueue;

    @Autowired
    public BatchEmailService(WorkflowClient workflowClient,
                             @Value("${temporal.taskqueue:BatchEmailTaskQueue}") String taskQueue) {
        this.workflowClient = workflowClient;
        this.taskQueue = taskQueue;
    }

    public void startWorkflow(String workflowId, BatchEmailController.BatchEmailSendRequest payload) {
        EmailCampaignWorkflow workflow = workflowClient.newWorkflowStub(
                EmailCampaignWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(taskQueue)
                        .setWorkflowId(workflowId)
                        .setWorkflowIdReusePolicy(WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_REJECT_DUPLICATE)
                        .build());

        // Asynchronously start the workflow and continue immediately
        WorkflowClient.start(workflow::startEmailCampaign, payload.emails);
        logger.info("Started startEmailCampaign workflow with ID: {}", workflowId);
    }

    /**
     * Checks the status of a Temporal workflow based on the workflowId.
     *
     * @param workflowId The ID of the workflow to check
     * @return A string representing the workflow status
     */
    public String checkWorkflowStatus(String workflowId) {
        try {
            WorkflowExecution workflowExecution = WorkflowExecution.newBuilder()
                    .setWorkflowId(workflowId)
                    .build();

            WorkflowExecutionInfo execution = workflowClient.getWorkflowServiceStubs()
                    .blockingStub()
                    .describeWorkflowExecution(
                            DescribeWorkflowExecutionRequest.newBuilder()
                                    .setNamespace(workflowClient.getOptions().getNamespace())
                                    .setExecution(workflowExecution)
                                    .build())
                    .getWorkflowExecutionInfo();

            WorkflowExecutionStatus status = execution.getStatus();
            return status.toString();
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                return "NOT_FOUND";
            }
            throw e;
        }
    }
}