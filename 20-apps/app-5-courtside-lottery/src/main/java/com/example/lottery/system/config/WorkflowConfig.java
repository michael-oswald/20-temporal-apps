package com.example.lottery.system.config;

import com.example.lottery.system.workflows.LotteryManagerWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkflowConfig {

    private final WorkflowClient workflowClient;
    private static final Logger log = LoggerFactory.getLogger(WorkflowConfig.class);

    public WorkflowConfig(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @PostConstruct
    public void startSeatManagerWorkflow() {
        LotteryManagerWorkflow lotteryManagerWorkflow = workflowClient.newWorkflowStub(
                LotteryManagerWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setWorkflowId("LotteryManagerWorkflow")
                        .setTaskQueue("LOTTERY_TASK_QUEUE")
                        .build()
        );

        try {
            WorkflowClient.start(lotteryManagerWorkflow::start);
            log.info("LotteryManagerWorkflow started");
        } catch (io.temporal.client.WorkflowExecutionAlreadyStarted e) {
            // Workflow is already running, safe to ignore
        }
    }
}