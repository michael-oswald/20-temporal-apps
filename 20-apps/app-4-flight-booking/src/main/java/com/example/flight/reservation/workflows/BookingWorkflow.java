package com.example.flight.reservation.workflows;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface BookingWorkflow {
    @WorkflowMethod
    void book(String userId, String seatId);
}