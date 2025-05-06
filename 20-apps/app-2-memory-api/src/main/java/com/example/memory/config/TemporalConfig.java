package com.example.memory.config;

import com.example.memory.activities.DynamoDbActivityImpl;
import com.example.memory.workflows.MemoryWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalConfig {

    @Value("${temporal.server.address:127.0.0.1:7233}")
    private String temporalServerAddress;

    @Value("${temporal.namespace:default}")
    private String temporalNamespace;

    @Value("${temporal.taskqueue:MemoryTaskQueue}")
    private String taskQueue;

    @Value("${temporal.reminder.taskqueue:ReminderTaskQueue}")
    private String reminderTaskQueue;

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        return WorkflowServiceStubs.newInstance(
                                WorkflowServiceStubsOptions.newBuilder()
                                        .setTarget(temporalServerAddress)
                                        .build());
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs workflowServiceStubs) {
        return WorkflowClient.newInstance(workflowServiceStubs,
                WorkflowClientOptions.newBuilder()
                        .setNamespace(temporalNamespace)
                        .build());
    }

    @Bean
    public WorkerFactory workerFactory(WorkflowClient workflowClient) {
        return WorkerFactory.newInstance(workflowClient);
    }

    @Bean
    public Worker memoryWorker(WorkerFactory workerFactory, DynamoDbActivityImpl dynamoDbActivity) {
        Worker worker = workerFactory.newWorker(taskQueue);
        worker.registerWorkflowImplementationTypes(MemoryWorkflowImpl.class);
        worker.registerActivitiesImplementations(dynamoDbActivity);
        workerFactory.start();
        return worker;
    }
}