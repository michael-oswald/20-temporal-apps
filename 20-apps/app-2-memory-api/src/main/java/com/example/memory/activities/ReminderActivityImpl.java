package com.example.memory.activities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class ReminderActivityImpl implements ReminderActivity {
    private static final Logger logger = LoggerFactory.getLogger(ReminderActivityImpl.class);

    @Override
    public void checkAndSendReminders() {
        logger.info("Checking for reminders to send...");

        // todo implement an efficient query for all reminders
        // and send them out via email or something..

        logger.info("Reminder check completed.");
    }
}