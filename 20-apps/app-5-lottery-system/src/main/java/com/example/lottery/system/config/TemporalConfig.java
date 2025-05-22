package com.example.lottery.system.config;


import com.example.lottery.system.workflows.LotteryManagerWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLException;

@Configuration
public class TemporalConfig {

    @Value("${temporal.server.address:127.0.0.1:7233}")
    private String temporalServerAddress;

    @Value("${temporal.namespace:default}")
    private String temporalNamespace;

    @Value("${temporal.api.key:}")
    private String temporalApiKey;

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() throws SSLException {
/*
        if (temporalApiKey != null && !temporalApiKey.isEmpty()) {
            WorkflowServiceStubsOptions options = WorkflowServiceStubsOptions.newBuilder()
                    .setTarget(temporalServerAddress)
                    .setSslContext(GrpcSslContexts.forClient().build())
                    .setGrpcMetadataProviders(List.of(new TemporalApiKeyProvider(temporalApiKey)))
                    .build();

            return WorkflowServiceStubs.newServiceStubs(options);
        }*/

        return WorkflowServiceStubs.newServiceStubs(
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
        WorkerFactory workerFactory = WorkerFactory.newInstance(workflowClient);

        Worker seatWorker = workerFactory.newWorker("LOTTERY_TASK_QUEUE");
        seatWorker.registerWorkflowImplementationTypes(LotteryManagerWorkflowImpl.class);

        workerFactory.start();
        return workerFactory;
    }
}