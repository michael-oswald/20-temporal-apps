package com.example.flight.reservation.workflows;

import com.example.flight.reservation.activities.BookingActivity;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class BookingWorkflowImpl implements BookingWorkflow{

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
        Promise<Void> seatPromise = Async.procedure(seatManager::requestSeat, userId);
        seatPromise.get(); // Wait for completion, yields control

        Workflow.await(Duration.ofMinutes(15), () -> paymentReceived || userCancelled);

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
