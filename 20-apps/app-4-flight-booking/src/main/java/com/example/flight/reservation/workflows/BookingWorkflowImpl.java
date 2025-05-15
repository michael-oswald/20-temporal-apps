package com.example.flight.reservation.workflows;

import com.example.flight.reservation.activities.BookingActivity;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class BookingWorkflowImpl implements BookingWorkflow{
    private static final Logger log = LoggerFactory.getLogger(BookingWorkflowImpl.class);
    private boolean paymentReceived = false;
    private boolean userCancelled = false;
    private final BookingActivity bookingActivities = Workflow.newActivityStub(
            BookingActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .build()
    );

    @Override
    public void book(String userId) {
        SeatManagerWorkflow seatManager = Workflow.newExternalWorkflowStub(
                SeatManagerWorkflow.class,
                "SeatManagerWorkflow"
        );

        // Send the signal asynchronously to avoid blocking

        try {
            Async.procedure(seatManager::requestSeat, userId);
            //Async.procedure(seatManager::requestSeat, userId).get();
        } catch (Exception e) {
            // Handle the exception if needed
            log.error("Seat request failed: " + e.getMessage(), e);
        }

        Workflow.await(Duration.ofMinutes(2), () -> paymentReceived || userCancelled);

        if (paymentReceived) {
            var seatId = bookingActivities.getHeldSeatFromSeatManager(userId);
            seatManager.confirmSeat(userId);
            bookingActivities.saveBookingToDatabase(userId, seatId);

        } else {
            seatManager.releaseSeat(userId);
        }
    }

    @Override
    public void paymentReceived() {
        this.paymentReceived = true;
    }

    @Override
    public void userCancelled() {
        this.userCancelled = true;
    }
}
