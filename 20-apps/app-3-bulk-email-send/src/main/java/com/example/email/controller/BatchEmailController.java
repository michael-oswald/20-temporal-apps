package com.example.email.controller;

import com.example.email.Email;
import com.example.email.service.BatchEmailService;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/email")
public class BatchEmailController {

    private static final Logger logger = LoggerFactory.getLogger(BatchEmailController.class);

    @Autowired
    private BatchEmailService batchEmailService;

    @PostMapping("/batch")
    public ResponseEntity<?> batchSend(@RequestBody @Valid BatchEmailController.BatchEmailSendRequest payload) {
        try {

            logger.info("Received payload: {}", payload);

            String workflowId = "workflowId-" + payload.uniqueEmailBatchId;

            // Start a Temporal workflow
            batchEmailService.startWorkflow(workflowId, payload);
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

    @GetMapping("/batch/status/{uniqueEmailBatchId}")
    public ResponseEntity<?> getBatchStatus(@PathVariable String uniqueEmailBatchId) {
        try {
            String workflowId = "workflowId-" + uniqueEmailBatchId;

            // Get workflow status
            String status = batchEmailService.checkWorkflowStatus(workflowId);

            logger.info("Checked workflow status for ID: {}, status: {}", workflowId, status);

            return ResponseEntity.ok().body(new WorkflowStatusResponse(status));
        } catch (Exception e) {
            logger.error("Error checking workflow status for id {}", uniqueEmailBatchId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking workflow status: " + e.getMessage());
        }
    }

    public static class WorkflowStatusResponse {
            private final String status;

            public WorkflowStatusResponse(String status) {
                this.status = status;
            }

            public String getStatus() {
                return status;
            }
        }


    public static class BatchEmailSendRequest {
        @NotNull(message = "emails is required")
        public List<Email> emails;
        @NotNull(message = "uniqueEmailBatchId is required")
        public String uniqueEmailBatchId;
    }
}
