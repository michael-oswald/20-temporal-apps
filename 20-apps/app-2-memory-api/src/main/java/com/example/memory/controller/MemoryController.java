package com.example.memory.controller;

import com.example.memory.service.MemoryService;
import com.example.memory.workflows.MemoryWorkflowImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/memory")
public class MemoryController {

    private static final Logger logger = LoggerFactory.getLogger(MemoryController.class);

    @Autowired
    private MemoryService memoryService;

    @PostMapping
    public ResponseEntity<?> postMemoryRequest(@RequestBody @Valid MemoryRequest payload) {
        try {

            logger.info("Received payload: {}", payload);

            String workflowId = "workflowId-" + payload.userId + payload.uniqueMemoryId;

            // Start a Temporal workflow
            memoryService.startWorkflow(workflowId, payload);
            logger.info("Started Temporal workflow with ID: {}", workflowId);

           return ResponseEntity.ok().build();
        } catch (WorkflowExecutionAlreadyStarted e) {
            logger.warn("Idempotency check hit for payload {}", payload);
            return ResponseEntity.ok().build();
        }
        catch (Exception e) {
            logger.error("Error processing request {}", payload, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing request: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}/{categoryId}")
    public ResponseEntity<?> getMemoryByUserAndCategory(@PathVariable String userId, @PathVariable String categoryId) {
        try {
            logger.info("Fetching memory for userId: {}, categoryId: {}", userId, categoryId);
            MemoryWorkflowImpl.Memory memory = memoryService.getMemoryByUserAndCategory(userId, categoryId);
            return ResponseEntity.ok(memory);
        } catch (Exception e) {
            logger.error("Error fetching memory for userId: {}, categoryId: {}", userId, categoryId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching memory: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getAllMemoriesByUserId(@PathVariable String userId) {
        try {
            logger.info("Fetching all memories for userId: {}", userId);
            var memories = memoryService.getAllMemoriesByUserId(userId);
            return ResponseEntity.ok(memories);
        } catch (Exception e) {
            logger.error("Error fetching memories for userId: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching memories: " + e.getMessage());
        }
    }



    public static class MemoryRequest {
        @NotNull(message = "userId is required")
        public String userId;
        @NotNull(message = "uniqueMemoryId is required")
        public String uniqueMemoryId;
        @NotNull(message = "text is required")
        public String text;
        @NotNull(message = "category is required")
        public String category;
        public LocalDateTime dueAt;
    }
}

