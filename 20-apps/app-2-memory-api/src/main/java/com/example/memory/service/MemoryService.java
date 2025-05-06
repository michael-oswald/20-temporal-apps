package com.example.memory.service;

import com.example.memory.controller.MemoryController;
import com.example.memory.workflows.MemoryWorkflow;
import com.example.memory.workflows.MemoryWorkflowImpl;
import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static com.example.memory.activities.DynamoDbActivityImpl.TABLE_NAME;

@Service
public class MemoryService {
    private static final Logger logger = LoggerFactory.getLogger(MemoryService.class);

    private final WorkflowClient workflowClient;
    private final String taskQueue;
    private final DynamoDbClient dynamoDbClient;

    @Autowired
    public MemoryService(WorkflowClient workflowClient,
                         @Value("${temporal.taskqueue:MemoryTaskQueue}") String taskQueue,
                         DynamoDbClient dynamoDbClient) {
        this.workflowClient = workflowClient;
        this.taskQueue = taskQueue;
        this.dynamoDbClient = dynamoDbClient;
    }

    public void startWorkflow(String workflowId, MemoryController.MemoryRequest payload) {
        MemoryWorkflow workflow = workflowClient.newWorkflowStub(
                MemoryWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(taskQueue)
                        .setWorkflowId(workflowId)
                        .setWorkflowIdReusePolicy(WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_REJECT_DUPLICATE)
                        .build());

        // Asynchronously start the workflow and continue immediately
        WorkflowClient.start(workflow::processWorkflow, payload);
        logger.info("Started memory workflow with ID: {}", workflowId);
    }

    public List<MemoryWorkflowImpl.Memory> getMemoryByUserAndCategory(String userId, String categoryId) {
        logger.info("Retrieving memory from DynamoDB for userId: {}, categoryId: {}", userId, categoryId);
        return getMemoryByUserIdAndCategoryId(userId, categoryId);
    }

    public List<MemoryWorkflowImpl.Memory> getMemoryByUserIdAndCategoryId(String userId, String categoryId) {
        // for now just get all memoriesByUserId
        var allMemories = getAllMemoriesByUserId(userId);

        // Filter the memories by categoryId
        return allMemories.stream()
                .filter(memory -> memory.getCategory().equals(categoryId))
                .toList();
    }


    public List<MemoryWorkflowImpl.Memory> getAllMemoriesByUserId(String userId) {
        logger.info("Retrieving all memories from DynamoDB for userId: {}", userId);

        // Query DynamoDB with only the partition key (userId)
        var expressionAttributeValues = new HashMap<String, AttributeValue>();
        expressionAttributeValues.put(":userId", AttributeValue.builder().s(userId).build());

        var queryRequest = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .keyConditionExpression("userId = :userId")
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        var queryResponse = dynamoDbClient.query(queryRequest);

        // Map the results to Memory objects
        var memories = new ArrayList<MemoryWorkflowImpl.Memory>();
        for (var item : queryResponse.items()) {
            var memory = new MemoryWorkflowImpl.Memory();
            memory.setUserId(item.get("userId").s());
            memory.setMemoryId(item.get("memoryId").s());
            memory.setText(item.get("text").s());
            memory.setCategory(item.get("category").s());
            memory.setNotes(item.get("notes") != null ? item.get("notes").s() : null);
            memory.setCreatedAt(item.get("createdAt").s());
            memory.setStatus(item.get("status").s());
            memory.setDueAt(item.get("dueAt") != null ? item.get("dueAt").s() : null);
            memories.add(memory);
        }

        return memories;
    }

    public void deleteMemoryByUserIdAndMemoryId(String userId, String memoryId) {
        try {
            logger.info("Deleting memory from DynamoDB for userId: {}, memoryId: {}", userId, memoryId);

            // Create key for the item to delete
            var key = new HashMap<String, AttributeValue>();
            key.put("userId", AttributeValue.builder().s(userId).build());
            key.put("memoryId", AttributeValue.builder().s(memoryId).build());

            // Create delete item request
            var deleteRequest = software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .key(key)
                    .build();

            // Execute delete operation
            dynamoDbClient.deleteItem(deleteRequest);

            logger.info("Successfully deleted memory for userId: {}, memoryId: {}", userId, memoryId);
        } catch (Exception e) {
            logger.error("Error deleting memory for userId: {}, memoryId: {}", userId, memoryId, e);
            throw e;
        }
    }

    public void updateMemoryByUserIdAndMemoryId(String userId, String memoryId, MemoryController.MemoryRequest payload) {
        try {
            logger.info("Updating memory in DynamoDB for userId: {}, memoryId: {}", userId, memoryId);

            // Create key for the item to update
            var key = new HashMap<String, AttributeValue>();
            key.put("userId", AttributeValue.builder().s(userId).build());
            key.put("memoryId", AttributeValue.builder().s(memoryId).build());

            // Create update expression and attribute values
            var updateExpression = new StringBuilder("SET ");
            var expressionAttributeValues = new HashMap<String, AttributeValue>();

            if (payload.text != null) {
                updateExpression.append("text = :text, ");
                expressionAttributeValues.put(":text", AttributeValue.builder().s(payload.text).build());
            }
            if (payload.category != null) {
                updateExpression.append("category = :category, ");
                expressionAttributeValues.put(":category", AttributeValue.builder().s(payload.category).build());
            }
            if (payload.dueAt != null) {
                updateExpression.append("dueAt = :dueAt, ");
                expressionAttributeValues.put(":dueAt", AttributeValue.builder().s(payload.dueAt.toString()).build());
            }
            if (payload.status != null) {
                updateExpression.append("status = :status, ");
                expressionAttributeValues.put(":status", AttributeValue.builder().s(payload.status.toString()).build());
            }

            // Remove trailing comma and space
            if (updateExpression.toString().endsWith(", ")) {
                updateExpression.setLength(updateExpression.length() - 2);
            }

            // Create update item request
            var updateRequest = software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .key(key)
                    .updateExpression(updateExpression.toString())
                    .expressionAttributeValues(expressionAttributeValues)
                    .build();

            // Execute update operation
            dynamoDbClient.updateItem(updateRequest);

            logger.info("Successfully updated memory for userId: {}, memoryId: {}", userId, memoryId);
        } catch (Exception e) {
            logger.error("Error updating memory for userId: {}, memoryId: {}", userId, memoryId, e);
            throw e;
        }
    }
}