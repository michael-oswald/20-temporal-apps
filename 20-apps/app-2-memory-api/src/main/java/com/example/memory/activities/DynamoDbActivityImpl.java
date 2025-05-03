package com.example.memory.activities;

import com.example.memory.workflows.MemoryWorkflowImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;

@Component
public class DynamoDbActivityImpl implements DynamoDbActivity {
    private static final Logger logger = LoggerFactory.getLogger(DynamoDbActivityImpl.class);
    public static final String TABLE_NAME = "gpt-memory";

    private final DynamoDbClient dynamoDbClient;

    @Autowired
    public DynamoDbActivityImpl(DynamoDbClient dynamoDbClient) {
        // Initialize DynamoDB client
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public void saveMemory(MemoryWorkflowImpl.Memory memory) {
        try {
            logger.info("Saving memory to DynamoDB, memoryId: {}", memory.getMemoryId());

            // Create item attributes
            var item = new HashMap<String, AttributeValue>();
            item.put("userId", AttributeValue.builder().s(memory.getUserId()).build());
            item.put("memoryId", AttributeValue.builder().s(memory.getMemoryId()).build());
            item.put("text", AttributeValue.builder().s(memory.getText()).build());
            item.put("category", AttributeValue.builder().s(memory.getCategory()).build());
            item.put("createdAt", AttributeValue.builder().s(memory.getCreatedAt()).build());
            item.put("dueAt", AttributeValue.builder().s(memory.getDueAt()).build());
            item.put("completed", AttributeValue.builder().bool(memory.isCompleted()).build());

            // Create put item request
            var request = PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(item)
                    .build();

            // Put item in DynamoDB
            dynamoDbClient.putItem(request);

            logger.info("Successfully saved memory item to DynamoDB");
        } catch (Exception e) {
            logger.error("Error saving memory item to DynamoDB", e);
            throw e;
        }
    }

}