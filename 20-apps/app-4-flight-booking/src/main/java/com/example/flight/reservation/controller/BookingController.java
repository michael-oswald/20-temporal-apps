package com.example.flight.reservation.controller;


import com.example.flight.reservation.workflows.BookingWorkflow;
import com.example.flight.reservation.workflows.SeatManagerWorkflow;
import io.grpc.StatusRuntimeException;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowQueryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseEntity<String> startBooking(@PathVariable String userId) {
        SeatManagerWorkflow seatManager = workflowClient.newWorkflowStub(
                SeatManagerWorkflow.class,
                "SeatManagerWorkflow"
        );
        List<String> availableSeats = seatManager.getAvailableSeats();
        if (availableSeats == null || availableSeats.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No seats available. Plane is full.");
        }

        BookingWorkflow bookingWorkflow = workflowClient.newWorkflowStub(
                BookingWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setWorkflowId("BookingWorkflow_" + userId)
                        .setTaskQueue("BOOKING_TASK_QUEUE")
                        .build()
        );

        try {
            WorkflowClient.start(bookingWorkflow::book, userId);
        } catch (io.temporal.client.WorkflowExecutionAlreadyStarted e) {
            // Workflow is already running, safe to ignore
        }
        return ResponseEntity.ok("Booking started for user " + userId);
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
        try {
            SeatManagerWorkflow seatManager = workflowClient.newWorkflowStub(
                    SeatManagerWorkflow.class,
                    "SeatManagerWorkflow"
            );
            return seatManager.getAvailableSeats();
        } catch (WorkflowQueryException | StatusRuntimeException | IllegalArgumentException e) {
            // Workflow is terminated or not running
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Seat manager workflow is not running", e);
        }
    }
}