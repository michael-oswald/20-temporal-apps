package com.example.adapter.controller;

import com.example.adapter.service.WebhookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/webhook")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    @Autowired
    private WebhookService webhookService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody String payload) {
        try {

            logger.info("Received webhook: {}", payload);
            WebhookRequest webhookRequest = objectMapper.readValue(payload, WebhookRequest.class);

            String workflowId = "workflowId-" + webhookRequest.webhookId;

            // Start a Temporal workflow
            webhookService.startWebhookWorkflow(workflowId, payload);
            logger.info("Started Temporal workflow with ID: {}", workflowId);

            return ResponseEntity.ok("Webhook received successfully");
        } catch (WorkflowExecutionAlreadyStarted e) {
            logger.warn("Idempotency check hit for payload {}", payload);
            return ResponseEntity.ok("Webhook received successfully");
        }
        catch (Exception e) {
            logger.error("Error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing webhook: " + e.getMessage());
        }
    }

    public static class WebhookRequest {
        public String webhookId;
        public String data;
        public String createdAt;
    }
}

