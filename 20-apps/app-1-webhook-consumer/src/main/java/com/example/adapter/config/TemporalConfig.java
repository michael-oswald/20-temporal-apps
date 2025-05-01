package com.example.adapter.config;

import com.example.adapter.activities.WebhookDynamoDbActivityImpl;
import com.example.adapter.workflows.WebhookWorkflowImpl;
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

    @Value("${temporal.taskqueue:WebhookTaskQueue}")
    private String taskQueue;

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
    public Worker worker(WorkerFactory workerFactory, WebhookDynamoDbActivityImpl dynamoDbActivity) {
        Worker worker = workerFactory.newWorker(taskQueue);
        worker.registerWorkflowImplementationTypes(WebhookWorkflowImpl.class);
        worker.registerActivitiesImplementations(dynamoDbActivity);
        workerFactory.start();
        return worker;
    }
}