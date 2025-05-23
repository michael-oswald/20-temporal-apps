package com.example.lottery.system.config;


import com.example.lottery.system.workflows.LotteryManagerWorkflowImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.SimpleSslContextBuilder;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import javax.net.ssl.SSLException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class TemporalConfig {

    @Value("${temporal.server.address:127.0.0.1:7233}")
    private String temporalServerAddress;

    @Value("${temporal.namespace:default}")
    private String temporalNamespace;

    @Bean
    @Order(0)
    public WorkflowServiceStubs workflowServiceStubs() throws SSLException, JsonProcessingException {

        // check if running local:
        if (temporalServerAddress.equals("127.0.0.1:7233")) {
            WorkflowServiceStubsOptions stubsOptions = WorkflowServiceStubsOptions
                    .newBuilder()
                    .setTarget(temporalServerAddress)
                    .build();

            return WorkflowServiceStubs.newServiceStubs(stubsOptions);
        }

        // else: below is for running in aws:

        Region region = Region.of("us-east-1");

        // Create a Secrets Manager client
        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(region)
                .build();

        String clientKeySecret = "my-temporal-client.key";
        GetSecretValueRequest clientKeyRequest = GetSecretValueRequest.builder()
                .secretId(clientKeySecret)
                .build();

        String clientPemSecret = "my-temporal-client.pem";
        GetSecretValueRequest clientPemRequest = GetSecretValueRequest.builder()
                .secretId(clientPemSecret)
                .build();

        String key = client.getSecretValue(clientKeyRequest).secretString();
        String pem = client.getSecretValue(clientPemRequest).secretString();

        InputStream clientCertInputStream = new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8));
        InputStream clientKeyInputStream = new ByteArrayInputStream(key.getBytes(StandardCharsets.UTF_8));
        SslContext sslContext = SimpleSslContextBuilder.forPKCS8(clientCertInputStream, clientKeyInputStream).build();

        GetSecretValueRequest temporalAddressesRequest = GetSecretValueRequest.builder()
                .secretId("temporal-addresses")
                .build();
        String addresses = client.getSecretValue(temporalAddressesRequest).secretString();
        // Parse the addresses as a JSON object using Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(addresses);
        this.temporalServerAddress = jsonNode.get("temporal-address").asText();
        this.temporalNamespace = jsonNode.get("temporal-namespace").asText();

        WorkflowServiceStubsOptions stubsOptions = WorkflowServiceStubsOptions
                .newBuilder()
                .setSslContext(sslContext)
                .setTarget(temporalServerAddress)
                .build();

        return WorkflowServiceStubs.newServiceStubs(stubsOptions);
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