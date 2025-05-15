package com.example.flight.reservation.config;

import com.example.flight.reservation.workflows.SeatManagerWorkflow;
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
        SeatManagerWorkflow seatManager = workflowClient.newWorkflowStub(
                SeatManagerWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setWorkflowId("SeatManagerWorkflow")
                        .setTaskQueue("SEAT_TASK_QUEUE")
                        .build()
        );

        try {
            WorkflowClient.start(seatManager::start);
            log.info("SeatManagerWorkflow started");
        } catch (io.temporal.client.WorkflowExecutionAlreadyStarted e) {
            // Workflow is already running, safe to ignore
        }
    }
}