package com.example.flight.reservation.controller;


import com.example.flight.reservation.workflows.SeatManagerWorkflow;
import io.grpc.StatusRuntimeException;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowQueryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/booking")
public class BookingController {

    private final WorkflowClient workflowClient;

    public BookingController(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @PostMapping("/book/{userId}")
    public ResponseEntity<String> startBooking(@PathVariable String userId) {
        SeatManagerWorkflow seatManager = workflowClient.newWorkflowStub(
                SeatManagerWorkflow.class, "SeatManagerWorkflow"
        );
        List<String> availableSeats = seatManager.getAvailableSeats();
        if (availableSeats.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Booking failed: Plane is full. No seats available.");
        }
        seatManager.requestBooking(userId);
        return ResponseEntity.ok("Booking request submitted for user " + userId);
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

    @GetMapping("/failed")
    public ResponseEntity<Map<String, String>> getAllFailedBookings() {
        SeatManagerWorkflow seatManager = workflowClient.newWorkflowStub(
            SeatManagerWorkflow.class, "SeatManagerWorkflow"
        );
        Map<String, String> failed = seatManager.getFailedBookings();
        return ResponseEntity.ok(failed);
    }
}