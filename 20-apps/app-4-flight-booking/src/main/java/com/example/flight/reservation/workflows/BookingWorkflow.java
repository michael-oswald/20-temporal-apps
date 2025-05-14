package com.example.flight.reservation.workflows;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import io.temporal.workflow.SignalMethod;

@WorkflowInterface
public interface BookingWorkflow {
    @WorkflowMethod
    void book(String userId);

    @SignalMethod
    void paymentReceived();

    @SignalMethod
    void userCancelled();
}
