package com.example.memory.controller;

import com.example.memory.Email;
import com.example.memory.service.MemoryService;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/memory")
public class MemoryController {

    private static final Logger logger = LoggerFactory.getLogger(MemoryController.class);

    @Autowired
    private MemoryService memoryService;

    @PostMapping
    public ResponseEntity<?> postMemoryRequest(@RequestBody @Valid MemoryController.BatchEmailSendRequest payload) {
        try {

            logger.info("Received payload: {}", payload);

            String workflowId = "workflowId-" + payload.uniqueEmailBatchId;

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

    public static class BatchEmailSendRequest {
        @NotNull(message = "emails is required")
        public List<Email> emails;
        @NotNull(message = "uniqueEmailBatchId is required")
        public String uniqueEmailBatchId;
    }
}
