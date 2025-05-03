package com.example.memory.workflows;

import com.example.memory.activities.DynamoDbActivity;
import com.example.memory.controller.MemoryController;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.Instant;

import static com.example.memory.MemoryStatus.PENDING;

public class MemoryWorkflowImpl implements MemoryWorkflow {
    private final Logger logger = Workflow.getLogger(MemoryWorkflowImpl.class);

    private final DynamoDbActivity dynamoDbActivity =
            Workflow.newActivityStub(DynamoDbActivity.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(Duration.ofSeconds(10))
                            .build());

    @Override
    public void processWorkflow(MemoryController.MemoryRequest memoryRequest) {
        logger.info("Processing memory in workflow: {}", memoryRequest);

        Memory memory = new Memory();
        memory.setUserId(memoryRequest.userId);
        memory.setMemoryId(memoryRequest.uniqueMemoryId);
        memory.setText(memoryRequest.text);
        memory.setCategory(memoryRequest.category);
        memory.setStatus(PENDING);
        memory.setCreatedAt(Instant.now().toString());
        memory.setDueAt(memoryRequest.dueAt == null ? null : memoryRequest.dueAt.toString());

        // Save memory to DynamoDB
        dynamoDbActivity.saveMemory(memory);

        logger.info("memory request processing completed");
    }

    public static class Memory {
        private String userId;
        private String memoryId;
        private String text;
        private String category;
        private String createdAt;
        private String dueAt;
        private String notes;
        private String status;

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        // Getters and setters
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getMemoryId() {
            return memoryId;
        }

        public void setMemoryId(String memoryId) {
            this.memoryId = memoryId;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getDueAt() {
            return dueAt;
        }

        public void setDueAt(String dueAt) {
            this.dueAt = dueAt;
        }
    }
}