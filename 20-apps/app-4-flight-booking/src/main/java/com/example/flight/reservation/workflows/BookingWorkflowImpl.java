package com.example.flight.reservation.workflows;

import com.example.flight.reservation.activities.BookingActivity;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class BookingWorkflowImpl implements BookingWorkflow {
    private static final Logger log = LoggerFactory.getLogger(BookingWorkflowImpl.class);
    private final BookingActivity bookingActivities = Workflow.newActivityStub(
            BookingActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .build()
    );

    @Override
    public void book(String userId, String seatId) {
        try {
            bookingActivities.saveBookingToDatabase(userId, seatId);
        } catch (Exception e) {
            log.error("Booking failed: " + e.getMessage(), e);
        }
    }
}