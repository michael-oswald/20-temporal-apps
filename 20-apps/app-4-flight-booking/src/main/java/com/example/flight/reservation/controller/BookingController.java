package com.example.flight.reservation.controller;


import com.example.flight.reservation.workflows.BookingWorkflow;
import com.example.flight.reservation.workflows.SeatManagerWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/booking")
public class BookingController {

    private final WorkflowClient workflowClient;

    public BookingController(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @PostMapping("/start/{userId}")
    public String startBooking(@PathVariable String userId) {
        BookingWorkflow bookingWorkflow = workflowClient.newWorkflowStub(
                BookingWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setWorkflowId("BookingWorkflow_" + userId)
                        .setTaskQueue("BOOKING_TASK_QUEUE")
                        .build()
        );

        SeatManagerWorkflow seatManager = workflowClient.newWorkflowStub(
                SeatManagerWorkflow.class,
                WorkflowOptions.newBuilder().setWorkflowId("SeatManagerWorkflow")
                        .setTaskQueue("SEAT_TASK_QUEUE")
                        .build()
        );

        WorkflowClient.start(bookingWorkflow::book, userId);

        try {
            WorkflowClient.start(seatManager::start);
        } catch (io.temporal.client.WorkflowExecutionAlreadyStarted e) {
            // Workflow is already running, safe to ignore
        }
        return "Booking started for user " + userId;
    }

    @PostMapping("/pay/{userId}")
    public String completePayment(@PathVariable String userId) {
        BookingWorkflow workflow = workflowClient.newWorkflowStub(
                BookingWorkflow.class,
                "BookingWorkflow_" + userId
        );
        workflow.paymentReceived();
        return "Payment received for " + userId;
    }

    @PostMapping("/cancel/{userId}")
    public String cancelBooking(@PathVariable String userId) {
        BookingWorkflow workflow = workflowClient.newWorkflowStub(
                BookingWorkflow.class,
                "BookingWorkflow_" + userId
        );
        workflow.userCancelled();
        return "Booking cancelled for " + userId;
    }

    @GetMapping("/seats/available")
    public List<String> getAvailableSeats() {
        SeatManagerWorkflow seatManager = workflowClient.newWorkflowStub(
                SeatManagerWorkflow.class,
                "SeatManagerWorkflow"
        );
        return seatManager.getAvailableSeats();
    }
}