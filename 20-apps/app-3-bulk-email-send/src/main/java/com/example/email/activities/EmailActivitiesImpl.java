package com.example.email.activities;

import com.example.email.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.time.Instant;
import java.util.Map;

public class EmailActivitiesImpl implements EmailActivities {

    private final EmailService emailService; // Your SES, SendGrid, etc. abstraction
    private final DynamoDbClient dynamoDb;
    private final String tableName = "sent-emails";
    private static final Logger logger = LoggerFactory.getLogger(EmailActivitiesImpl.class);

    public EmailActivitiesImpl(EmailService emailService, DynamoDbClient dynamoDb) {
        this.emailService = emailService;
        this.dynamoDb = dynamoDb;
    }

    @Override
    public void sendEmail(String emailId, String to, String body) {
        // 1. Try to reserve the email ID (insert if not already exists)
        try {
            dynamoDb.putItem(PutItemRequest.builder()
                    .tableName(tableName)
                    .item(Map.of(
                            "emailId", AttributeValue.fromS(emailId),
                            "status", AttributeValue.fromS("RESERVED"),
                            "to", AttributeValue.fromS(to),
                            "sentAt", AttributeValue.fromS(Instant.now().toString())
                    ))
                    .conditionExpression("attribute_not_exists(emailId)") // dedupe key
                    .build()
            );
        } catch (ConditionalCheckFailedException e) {
            // Already sent — no-op for idempotency
            return;
        }

        // 2. Send the actual email
        try {
            emailService.send(to, body); // external side-effect
        } catch (Exception e) {
            // Optionally: mark as FAILED or clean up the reserved entry
            logger.error("Failed to send email: {}", e.getMessage());
            throw e; // Let Temporal retry
        }

        // 3. Update status to SENT
        dynamoDb.updateItem(UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("emailId", AttributeValue.fromS(emailId)))
                .updateExpression("SET #s = :sent, sentAt = :now")
                .expressionAttributeNames(Map.of("#s", "status"))
                .expressionAttributeValues(Map.of(
                        ":sent", AttributeValue.fromS("SENT"),
                        ":now", AttributeValue.fromS(Instant.now().toString())
                ))
                .build()
        );
    }
}

