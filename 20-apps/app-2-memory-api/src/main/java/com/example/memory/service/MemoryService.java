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

    public MemoryWorkflowImpl.Memory getMemoryByUserAndCategory(String userId, String categoryId) {
        logger.info("Retrieving memory from DynamoDB for userId: {}, categoryId: {}", userId, categoryId);
        return getMemory(userId, categoryId);
    }

    public MemoryWorkflowImpl.Memory getMemory(String userId, String categoryId) {
        // Logic to retrieve memory from DynamoDB
        var key = new HashMap<String, AttributeValue>();
        key.put("userId", AttributeValue.builder().s(userId).build());
        key.put("categoryId", AttributeValue.builder().s(categoryId).build());

        GetItemRequest request = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .build();

        var item = dynamoDbClient.getItem(request).item();
        if (item == null || item.isEmpty()) {
            throw new RuntimeException("Memory not found for userId: " + userId + ", categoryId: " + categoryId);
        }

        var memory = new MemoryWorkflowImpl.Memory();
        memory.setUserId(item.get("userId").s());
        memory.setMemoryId(item.get("memoryId").s());
        memory.setText(item.get("text").s());
        memory.setCategory(item.get("category").s());
        memory.setCreatedAt(item.get("createdAt").s());
        memory.setDueAt(item.get("dueAt") != null ? item.get("dueAt").s() : null);

        return memory;
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
}