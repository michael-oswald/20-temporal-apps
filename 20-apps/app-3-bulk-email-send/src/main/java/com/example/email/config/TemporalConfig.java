package com.example.email.config;


import com.example.email.activities.EmailActivitiesImpl;
import com.example.email.service.EmailService;
import com.example.email.workflows.EmailBatchWorkflowImpl;
import com.example.email.workflows.EmailCampaignWorkflowImpl;
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

    @Value("${temporal.taskqueue:BatchEmailTaskQueue}")
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
    public Worker worker(WorkerFactory workerFactory, EmailActivitiesImpl emailActivities) {
        Worker worker = workerFactory.newWorker(taskQueue);
        worker.registerWorkflowImplementationTypes(EmailCampaignWorkflowImpl.class, EmailBatchWorkflowImpl.class);
        worker.registerActivitiesImplementations(emailActivities);
        workerFactory.start();
        return worker;
    }

    @Bean
    public EmailActivitiesImpl emailActivities(EmailService emailService) {
        return new EmailActivitiesImpl(emailService);
    }
}