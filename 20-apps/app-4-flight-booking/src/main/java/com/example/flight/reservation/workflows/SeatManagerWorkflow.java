package com.example.flight.reservation.workflows;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;
import java.util.Map;

@WorkflowInterface
public interface
SeatManagerWorkflow {

    @WorkflowMethod
    void start();

    @QueryMethod
    List<String> getAvailableSeats();

    @QueryMethod
    Map<String, String> getSeatStatus();

    @SignalMethod
    void requestBooking(String userId);

    @QueryMethod
    Map<String, String> getFailedBookings();
}
