package com.example.flight.reservation.activities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;

public class BookingActivityImpl implements BookingActivity {

    private static final Logger logger = LoggerFactory.getLogger(BookingActivityImpl.class);
    private static final String TABLE_NAME = "AirlineBookings";

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Override
    public void saveBookingToDatabase(String userId, String seatId) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("userId", AttributeValue.builder().s(userId).build());
        item.put("seatId", AttributeValue.builder().s(seatId).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
        logger.info("Persisted booking to DynamoDB: user={} seat={}", userId, seatId);
    }
}
