package com.example.adapter.activities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

@Component
public class WebhookDynamoDbActivityImpl implements WebhookDynamoDbActivity {
    private static final Logger logger = LoggerFactory.getLogger(WebhookDynamoDbActivityImpl.class);
    private static final String TABLE_NAME = "example_webhooks";

    private final DynamoDbClient dynamoDbClient;

    @Autowired
    public WebhookDynamoDbActivityImpl(DynamoDbClient dynamoDbClient) {
        // Initialize DynamoDB client
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public void saveWebhookToDynamoDb(String payload, String workflowId) {
        try {
            logger.info("Saving webhook to DynamoDB, workflowId: {}", workflowId);

            // Create item attributes
            var item = new HashMap<String, AttributeValue>();
            item.put("id", AttributeValue.builder().s(UUID.randomUUID().toString()).build());
            item.put("workflowId", AttributeValue.builder().s(workflowId).build());
            item.put("timestamp", AttributeValue.builder().n(String.valueOf(Instant.now().getEpochSecond())).build());
            item.put("payload", AttributeValue.builder().s(payload).build());

            // Create put item request
            var request = PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(item)
                    .build();

            // Put item in DynamoDB
            dynamoDbClient.putItem(request);

            logger.info("Successfully saved webhook to DynamoDB");
        } catch (Exception e) {
            logger.error("Error saving webhook to DynamoDB", e);
            throw e;
        }
    }
}