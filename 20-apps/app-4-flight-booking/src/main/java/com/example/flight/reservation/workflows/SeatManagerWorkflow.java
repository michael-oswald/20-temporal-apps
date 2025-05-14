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

    @SignalMethod
    void requestSeat(String userId);

    @SignalMethod
    void confirmSeat(String userId);

    @SignalMethod
    void releaseSeat(String userId);

    @QueryMethod
    List<String> getAvailableSeats();

    @QueryMethod(name = "getHeldSeat")
    String getHeldSeat(String userId);

    @QueryMethod
    Map<String, String> getSeatStatus();
}
